package com.fresconews.fresco.v2.views;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.exomedia.core.exoplayer.EMExoPlayer;
import com.devbrackets.android.exomedia.core.listener.ExoPlayerListener;
import com.devbrackets.android.exomedia.type.MediaSourceType;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.rx.RxBus;
import com.fresconews.fresco.messages.ActivePostChangedMessage;
import com.fresconews.fresco.v2.utils.ExoPlayerUtils;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.google.android.exoplayer.ExoPlayer;

import javax.inject.Inject;

import rx.Subscription;

public class FrescoPostVideoView extends TextureView implements TextureView.SurfaceTextureListener, View.OnClickListener, ExoPlayerListener {
    private static final String TAG = FrescoPostVideoView.class.getSimpleName();

    private String postId = null;
    private String videoUrl = null;

    private boolean muted = true;
    private boolean active = false;
    private boolean surfaceAvailable;
    private boolean listenerAdded = false;

    private Subscription activePostSubscription = null;

    @Inject
    AnalyticsManager analyticsManager;

    //<editor-fold desc="Constructors">
    public FrescoPostVideoView(Context context) {
        super(context);
        setup();
    }

    public FrescoPostVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public FrescoPostVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    private void setup() {
        ((Fresco2) Fresco2.getContext().getApplicationContext()).getFrescoComponent().inject(this);
        setSurfaceTextureListener(this);
        setOnClickListener(this);
    }
    //</editor-fold>

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (activePostSubscription != null && !activePostSubscription.isUnsubscribed()) {
            activePostSubscription.unsubscribe();
        }

        activePostSubscription = RxBus.getInstance().register(ActivePostChangedMessage.class, message -> {
            if (TextUtils.isEmpty(videoUrl)) {
                return;
            }

            // postId can be empty when it comes from submission
            if (TextUtils.isEmpty(postId) || (!TextUtils.isEmpty(postId) && postId.equals(message.postId))) {
                active = true;
                startPlayback();
            }
            else {
                active = false;
                stopPlayback();
            }
        });
    }

    public void bind(String videoUrl, String postId) {
        if (videoUrl == null) {
            return;
        }

        this.postId = postId;
        this.videoUrl = videoUrl;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (activePostSubscription != null && !activePostSubscription.isUnsubscribed()) {
            activePostSubscription.unsubscribe();
        }

        Fresco2.mediaPlayer.setBufferUpdateListener(null);
    }

    public void startPlayback() {
        if (!surfaceAvailable || !active || TextUtils.isEmpty(videoUrl)) {
            return;
        }

        setAlpha(0.0f);

        EMExoPlayer mediaPlayer = Fresco2.mediaPlayer;
        mediaPlayer.seekTo(0);
        Uri uri = Uri.parse(videoUrl);
        mediaPlayer.setSurface(new Surface(getSurfaceTexture()));
        mediaPlayer.replaceRenderBuilder(ExoPlayerUtils.getRendererBuilder(MediaSourceType.getByLooseComparison(uri), uri));
        mediaPlayer.setPlayWhenReady(true);
        mediaPlayer.forcePrepare();

        if (!listenerAdded) {
            listenerAdded = true;
            mediaPlayer.addListener(this);
        }
        setMuted(true);
    }

    private void stopPlayback() {
        setMuted(true);
        if (listenerAdded) {
            listenerAdded = false;
            Fresco2.mediaPlayer.removeListener(this);
        }
    }

    private void setMuted(boolean muted) {
        if (videoUrl == null) {
            return;
        }
        if (this.muted != muted) { //change in muting
            analyticsManager.postSessionMute(muted);
        }
        this.muted = muted;
        EMExoPlayer mediaPlayer = Fresco2.mediaPlayer;

        if (this.muted) {
            mediaPlayer.setVolume(0);
        }
        else {
            mediaPlayer.setVolume(1);
        }

        View muteIcon = ((ViewGroup) getParent()).findViewById(R.id.mute_icon);
        if (muteIcon != null) {
            muteIcon.setVisibility(this.muted ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void onClick(View view) {
        if (!TextUtils.isEmpty(postId)) {
            post(() -> Fresco2.setActivePostIdInViewAnalytics(postId, null, true));
        }
        active = true;

        if (Fresco2.mediaPlayer.getPlaybackState() != ExoPlayer.STATE_READY) {
            startPlayback();
        }
        setMuted(!muted);
    }

    //<editor-fold desc="Texture Listeners">
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        surfaceAvailable = true;
        startPlayback();

        if (active) {
            Fresco2.mediaPlayer.setSurface(new Surface(surfaceTexture));
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        surfaceAvailable = false;
        Fresco2.mediaPlayer.blockingClearSurface();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
    //</editor-fold>

    //<editor-fold desc="ExoPlayer Listener">
    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        LogUtils.i(TAG, "onStateChanged");
        EMExoPlayer mediaPlayer = Fresco2.mediaPlayer;
        if (muted) {
            mediaPlayer.setVolume(0);
        }
        switch (playbackState) {
            case ExoPlayer.STATE_ENDED:
                LogUtils.i(TAG, "Ended");
                surfaceAvailable = true;
                Fresco2.mediaPlayer.restart();
                mediaPlayer.setBufferUpdateListener(null);
                break;
            case ExoPlayer.STATE_BUFFERING:
                LogUtils.i(TAG, "Buffering");
                mediaPlayer.setBufferUpdateListener(percent -> LogUtils.d(TAG, "Buffering " + videoUrl + " : " + percent));
                break;
            case ExoPlayer.STATE_IDLE:
                LogUtils.i(TAG, "Idle");
                mediaPlayer.setBufferUpdateListener(null);
                break;
            case ExoPlayer.STATE_PREPARING:
                LogUtils.i(TAG, "Preparing");
                mediaPlayer.setBufferUpdateListener(null);
                break;
            case ExoPlayer.STATE_READY:
                LogUtils.i(TAG, "Ready");
                setAlpha(1.0f);
                mediaPlayer.setBufferUpdateListener(null);
                break;
        }
    }

    @Override
    public void onError(EMExoPlayer emExoPlayer, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees, float pixelWidthHeightRatio) {
        LogUtils.i(TAG, "onVideoSizeChanged(" + width + ", " + height + ", " + unAppliedRotationDegrees + ", " + pixelWidthHeightRatio + ")");
        Matrix transformMatrix = new Matrix();
        getTransform(transformMatrix);

        float xScale = (float) getWidth() / width;
        float yScale = (float) getHeight() / height;

        float scale = Math.max(xScale, yScale);

        xScale = scale / xScale;
        yScale = scale / yScale;

        float xCenter = (float) getWidth() / 2f;
        float yCenter = (float) getHeight() / 2f;

        transformMatrix.setScale(xScale, yScale, xCenter, yCenter);

        setTransform(transformMatrix);
    }

    @Override
    public void onSeekComplete() {

    }
    //</editor-fold>
}

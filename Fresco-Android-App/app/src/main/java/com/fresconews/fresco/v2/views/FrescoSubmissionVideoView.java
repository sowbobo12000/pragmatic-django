package com.fresconews.fresco.v2.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.exomedia.ui.widget.EMVideoView;
import com.fresconews.fresco.R;

public class FrescoSubmissionVideoView extends EMVideoView {
    private static final String TAG = FrescoSubmissionVideoView.class.getSimpleName();

    private boolean muted = true;

    //<editor-fold desc="Constructors">
    public FrescoSubmissionVideoView(Context context) {
        super(context);
        init();
    }

    public FrescoSubmissionVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FrescoSubmissionVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //</editor-fold>

    private void init() {
        setOnPreparedListener(() -> {
            start();
            setMuted(muted);
        });

        setOnCompletionListener(this::restart);
        setOnClickListener(view -> {
            if (isPlaying()) {
                setMuted(!muted);
            }
            else {
                restart();
                setMuted(false);
            }
        });
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (this.muted) {
            setVolume(0);
        }
        else {
            setVolume(1);
        }

        ViewGroup parent = (ViewGroup) getParent();
        parent.findViewById(R.id.mute_icon).setVisibility(this.muted ? View.VISIBLE : View.INVISIBLE);
    }
}

package com.fresconews.fresco.framework.databinding.adapters;

import android.databinding.BindingAdapter;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import com.devbrackets.android.exomedia.core.video.scale.ScaleType;
import com.devbrackets.android.exomedia.ui.widget.EMVideoView;
import com.fresconews.fresco.v2.views.FrescoPostVideoView;
import com.fresconews.fresco.v2.views.FrescoSubmissionVideoView;

public class VideoAdapters {
    private static final String TAG = VideoAdapters.class.getSimpleName();

    @BindingAdapter({"stream"})
    public static void bindStream(EMVideoView videoView, String uri) {
        if (uri == null || uri.isEmpty()) {
            return;
        }

        videoView.setVideoPath(uri);
        videoView.setOnPreparedListener(videoView::start);

        videoView.setVolume(0);

        videoView.setScaleType(ScaleType.CENTER_CROP);

    }

    @BindingAdapter({"streamSubmission"})
    public static void bindStream(FrescoSubmissionVideoView videoView, Uri uri) {
        if (uri == null) {
            return;
        }

        // If it's a video, load it. Otherwise, hide the view
        if (uri.toString().contains(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString())) {

            videoView.setVisibility(View.VISIBLE);

            videoView.setVideoPath(uri.toString());

            videoView.setScaleType(ScaleType.CENTER_CROP);
        }
        else {
            videoView.setVisibility(View.GONE);
        }
    }

    @BindingAdapter({"stream", "postId"})
    public static void bindStream(FrescoPostVideoView videoView, String uri, String postId) {
        if (uri == null || uri.isEmpty() || postId == null || postId.isEmpty()) {
            return;
        }

        videoView.bind(uri, postId);
    }
}

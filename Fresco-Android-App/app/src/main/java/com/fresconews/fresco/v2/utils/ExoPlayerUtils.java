package com.fresconews.fresco.v2.utils;

import android.net.Uri;
import android.os.Build;

import com.devbrackets.android.exomedia.core.builder.DashRenderBuilder;
import com.devbrackets.android.exomedia.core.builder.HlsRenderBuilder;
import com.devbrackets.android.exomedia.core.builder.RenderBuilder;
import com.devbrackets.android.exomedia.core.builder.SmoothStreamRenderBuilder;
import com.devbrackets.android.exomedia.type.MediaSourceType;
import com.fresconews.fresco.Fresco2;

/**
 * A couple of utility methods used when dealing with the EMExoPlayer
 */
public class ExoPlayerUtils {
    public static RenderBuilder getRendererBuilder(MediaSourceType renderType, Uri uri) {
        switch (renderType) {
            case HLS:
                return new HlsRenderBuilder(Fresco2.getContext().getApplicationContext(), getUserAgent(), uri.toString());
            case DASH:
                return new DashRenderBuilder(Fresco2.getContext().getApplicationContext(), getUserAgent(), uri.toString());
            case SMOOTH_STREAM:
                return new SmoothStreamRenderBuilder(Fresco2.getContext().getApplicationContext(), getUserAgent(), uri.toString());
            default:
                return new RenderBuilder(Fresco2.getContext().getApplicationContext(), getUserAgent(), uri.toString());
        }
    }

    public static String getUserAgent() {
        return String.format("FrescoVideoView %s / Android %s / %s", Build.DEVICE + " (" + Build.BOARD + ")", Build.VERSION.RELEASE, Build.MODEL);
    }
}

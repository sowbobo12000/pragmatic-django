package com.fresconews.fresco.v2.utils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;

import java.io.IOException;

public class MediaHelper {
    private static final String MIME_TYPE_AVC = "video/avc";

    public static Bitmap getThumbnailFromVideo(Uri uri, long timeMs) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        return retriever.getFrameAtTime(timeMs * 1000);
    }

    public static int getDuration(Uri uri) {
        return getMediaMetadataRetrieverPropertyInteger(uri, MediaMetadataRetriever.METADATA_KEY_DURATION, 0);
    }

    public static int getWidth(Uri uri) {
        return getMediaMetadataRetrieverPropertyInteger(uri, MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH, 0);
    }

    public static int getHeight(Uri uri) {
        return getMediaMetadataRetrieverPropertyInteger(uri, MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT, 0);
    }

    public static int getVideoBitRate(Uri uri) {
        return getMediaMetadataRetrieverPropertyInteger(uri, MediaMetadataRetriever.METADATA_KEY_BITRATE, 0);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int getRotation(Uri uri) {
        return getMediaMetadataRetrieverPropertyInteger(uri, MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION, 0);
    }

    public static int getMediaMetadataRetrieverPropertyInteger(Uri uri, int key, int defaultValue) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        String value = retriever.extractMetadata(key);

        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static int getIFrameInterval(Uri uri) {

        return getMediaFormatPropertyInteger(uri, MediaFormat.KEY_I_FRAME_INTERVAL, 1);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static int getFrameRate(Uri uri) {
        return getMediaFormatPropertyInteger(uri, MediaFormat.KEY_FRAME_RATE, 30);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static int getAACProfile(Uri uri) {
        return getMediaFormatPropertyInteger(uri, MediaFormat.KEY_AAC_PROFILE, 0);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static int getAudioSampleRate(Uri uri) {
        return getMediaFormatPropertyInteger(uri, MediaFormat.KEY_SAMPLE_RATE, 44100);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static int getAudioChannelsCount(Uri uri) {
        return getMediaFormatPropertyInteger(uri, MediaFormat.KEY_CHANNEL_COUNT, 2);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static int getAudioBitRate(Uri uri) {
        return getMediaFormatPropertyInteger(uri, MediaFormat.KEY_BIT_RATE, 192 * 1024);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static int getMediaFormatPropertyInteger(Uri uri, String key, int defaultValue) {
        int value = defaultValue;

        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(uri.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
            return value;
        }

        MediaFormat format = getTrackFormat(extractor, MIME_TYPE_AVC);
        extractor.release();

        if (format != null && format.containsKey(key)) {
            value = format.getInteger(key);
        }

        return value;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static MediaFormat getTrackFormat(MediaExtractor extractor, String mimeType) {
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String trackMimeType = format.getString(MediaFormat.KEY_MIME);
            if (mimeType.equals(trackMimeType)) {
                return format;
            }
        }

        return null;
    }
}

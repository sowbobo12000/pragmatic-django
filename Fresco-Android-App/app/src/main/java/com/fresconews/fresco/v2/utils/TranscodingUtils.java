package com.fresconews.fresco.v2.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import com.fresconews.fresco.Fresco2;

import net.ypresto.androidtranscoder.MediaTranscoder;
import net.ypresto.androidtranscoder.format.MediaFormatStrategy;

import java.io.File;
import java.io.IOException;

import static com.fresconews.fresco.v2.utils.FileUtils.IN_APP_IDENTIFIER;

/**
 * Created by wumau on 12/6/2016.
 */
public class TranscodingUtils {
    private static final String TAG = TranscodingUtils.class.getSimpleName();
    public static final double BIT_RATE_BUFFER = 0.15;
    private static final int DEFAULT_BITRATE = 17000000;
    /* the preferred bitrate to resolution of a phone differs by approximately .3 between different
     tiers of resolution. E.g. 17000000b at 1080p to 12000000b at 720p. 5/17 = .2941
     So the acceptable margin of file bitrate has been decided to be .15 */
    public static final int FRAME_RATE = 30;
    private static int cameraHeight = 0;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static int getPreferredTranscodedBitrate(Uri fileUri) {
        int fileHeight = MediaHelper.getHeight(fileUri);
        int fileBitrate = MediaHelper.getVideoBitRate(fileUri);

        if (fileUri.toString().contains(IN_APP_IDENTIFIER)) {
            return fileBitrate; //photo taken with in-app camera (added here just in case)
        }

        /* Check to see if phone can generally handle this bit rate. If it's an older phone the
        bitrate should be relative to its capability. Capability is measured by its camera height
        and preferred bitrate */

        if (cameraHeight != 0) {
            int preferredBitRate = getPreferredBitrate(cameraHeight, false);

            if (fileBitrate > preferredBitRate) { //if greater than and above margin
                if (MathUtils.approxEquals(fileBitrate, preferredBitRate, BIT_RATE_BUFFER)) {
                    return fileBitrate;
                }
                return preferredBitRate;
            }
            return fileBitrate; //if below preferred bit rate, we're fine
        }
        if (MathUtils.approxEquals(fileBitrate, getPreferredBitrate(fileHeight, false), BIT_RATE_BUFFER)) {
            return fileBitrate;
        }
        //worst case scenario, use the bitrate that accomodates this file. This will hopefully never get called
        return getPreferredBitrate(fileHeight, false);

    }

    //Gets the preferred bitrate given the device resolution capabilities and implied upload speed capabilities
    public static int getPreferredBitrate(int height, boolean setCameraHeight) {
        if (setCameraHeight) {
            cameraHeight = height;
        }
        CamcorderProfile cProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        if (cProfile == null) {
            return DEFAULT_BITRATE;
        }
        int bitrateHigh = cProfile.videoBitRate;

        cProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
        int bitrate1080 = cProfile.videoBitRate;

        cProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
        int bitrate720 = cProfile.videoBitRate;

        cProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        int bitrate480 = cProfile.videoBitRate;

        if (height > 1080) {
            return (int) (.8 * bitrateHigh);
        }
        else if (height == 1080) {
            return bitrate1080;
        }
        else if (height == 720) {
            return bitrate720;
        }
        else if (height == 480) {
            return bitrate480;
        }
        return bitrateHigh;
    }

    //Gets the preferred bitrate given the device resolution capabilities and implied upload speed capabilities
    public static int getPreferredBitrate(int cameraId, int height, boolean setCameraHeight) {
        if (setCameraHeight) {
            cameraHeight = height;
        }
        CamcorderProfile cProfile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
        if (cProfile == null) {
            return DEFAULT_BITRATE;
        }
        int bitrateHigh = cProfile.videoBitRate;

        cProfile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_1080P);
        int bitrate1080 = cProfile.videoBitRate;

        cProfile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
        int bitrate720 = cProfile.videoBitRate;

        cProfile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
        int bitrate480 = cProfile.videoBitRate;

        if (height > 1080) {
            return (int) (.8 * bitrateHigh);
        }
        else if (height == 1080) {
            return bitrate1080;
        }
        else if (height == 720) {
            return bitrate720;
        }
        else if (height == 480) {
            return bitrate480;
        }
        return bitrateHigh;
    }

    public static boolean isAcceptableBitrate(Uri fileUri) {
        if (fileUri.toString().contains(IN_APP_IDENTIFIER)) {
            return true; //photo taken with in-app camera
        }
        int fileBitrate = MediaHelper.getVideoBitRate(fileUri);

        return fileBitrate == getPreferredTranscodedBitrate(fileUri);
    }

    public static void transcode(Uri inputUri, Uri outputPath, int targetRate, MediaTranscoder.Listener listener) {
        try {
            MediaTranscoder.getInstance().transcodeVideo(inputUri.getPath(), outputPath.getPath(),
                    new FrescoTranscodingFormatStrategy(targetRate), listener);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String newPath(Uri uri) {
        String newPath = uri.getPath().replace(uri.getLastPathSegment(), "") + "Transcoded_" + uri.getLastPathSegment();
        return  newPath;

        //Below would more permanently solve Amanda Nelson's Transcoding issue but currently it just shouldn't transcode on retry
//        String newPath = Environment.getExternalStorageDirectory() + "/Transcoded_" + uri.getLastPathSegment();
//        LogUtils.i("FrescoUploadService", "old path- " + uri.getPath());
//        LogUtils.i("FrescoUploadService", "path 1- " + newPath);
//        try {
//            File file = new File(Fresco2.getContext().getFilesDir(), "Transcoded_" + uri.getLastPathSegment());
//            newPath = file.getAbsolutePath();
//            LogUtils.i("FrescoUploadService", "path 4- " + file.getAbsolutePath());
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return newPath;
    }

    /**
     * Checks if the app has permission to write to device storager
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            LogUtils.i("FrescoUploadService", "NO PERMISSION!!");
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        else {
            LogUtils.i("FrescoUploadService", "WE HAVE PERMISSION!!");

        }
    }

    private static class FrescoTranscodingFormatStrategy implements MediaFormatStrategy {
        private final int videoBitrate;

        public FrescoTranscodingFormatStrategy(int videoBitrate) {
            this.videoBitrate = videoBitrate;
        }

        @Override
        public MediaFormat createVideoOutputFormat(MediaFormat inputFormat) {
            int width = inputFormat.getInteger(MediaFormat.KEY_WIDTH);
            int height = inputFormat.getInteger(MediaFormat.KEY_HEIGHT);

            MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
            format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.H263Level40);
//                format.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel4);
//            }
            return format;
        }

        @Override
        public MediaFormat createAudioOutputFormat(MediaFormat inputFormat) {
            return null;
        }
    }

    public static void setCameraHeight(int height) {
        cameraHeight = height;
    }
}

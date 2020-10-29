package com.fresconews.fresco.v2.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ryan on 7/14/2016.
 */
public class ContentUtils {
    private static final int OTHER_TYPE = 0;
    private static final int IMAGE_TYPE = 1;
    private static final int VIDEO_TYPE = 2;

    public static File uriToInputStreamToCachedFile(Context context, Uri uri, String id) {
        //http://stackoverflow.com/questions/10854211/android-store-inputstream-in-file
        //Create a temporary cached file. MAKE SURE TO DELETE THIS, IT IS YOUR RESPONSIBILITY.
        File file = new File(context.getCacheDir(), id);
        try {
            InputStream in = context.getContentResolver().openInputStream(uri);
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("Content", "uriToInputStreamToCachedFile Failed - " + e.getMessage());
        }
        return file;

    }

    public static Uri writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        if (path == null) {
            String sdCard = Environment.getExternalStorageDirectory().getPath();
            if (TextUtils.isEmpty(sdCard)) {
                return null;
            }
            File picturesDir = new File(sdCard, "Pictures");
            if (!picturesDir.exists()) {
                if (!picturesDir.mkdirs()) {
                    return null;
                }
            }
            path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        }

        if (path == null) {
            return null;
        }
        return Uri.parse(path);
    }

    private static Uri convertGoogleDriveImageToUri(Context context, Uri uri) {
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            Bitmap bmp = BitmapFactory.decodeStream(is);
            return writeToTempImageAndGetPathUri(context, bmp);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (is != null) {
                    is.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getMediaType(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver != null) {
            return contentResolver.getType(uri);
        }
        return null;
    }

    public static String getMediaPath(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        String path = null;
        int mediaType;
        String type = getMediaType(context, uri);
        if (type != null && type.contains("image")) {
            mediaType = IMAGE_TYPE;
        }
        else if (type != null && type.contains("video")) {
            mediaType = VIDEO_TYPE;
        }
        else {
            mediaType = OTHER_TYPE;
        }

        if (uri.getAuthority() != null && !uri.getAuthority().equals("media")) {
            if (mediaType == IMAGE_TYPE) {
                uri = convertGoogleDriveImageToUri(context, uri);
            }
            else if (mediaType == VIDEO_TYPE) {
                File file = uriToInputStreamToCachedFile(context, uri, "VID_" + System.currentTimeMillis());
                if (file != null) {
                    uri = Uri.fromFile(file);
                    return uri.getPath();
                }
            }
        }
        if (uri != null) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                String id = cursor.getString(0);
                id = id.substring(id.lastIndexOf(":") + 1);
                cursor.close();
                Cursor fileCursor = null;
                if (mediaType == IMAGE_TYPE) {
                    fileCursor = context.getContentResolver().query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{id}, null);
                    if (fileCursor != null) {
                        fileCursor.moveToFirst();
                        path = fileCursor.getString(fileCursor.getColumnIndex(MediaStore.Images.Media.DATA));

                    }
                }
                else if (mediaType == VIDEO_TYPE) {
                    fileCursor = context.getContentResolver().query(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Video.Media._ID + " = ? ", new String[]{id}, null);
                    if (fileCursor != null) {
                        fileCursor.moveToFirst();
                        path = fileCursor.getString(fileCursor.getColumnIndex(MediaStore.Video.Media.DATA));
                    }
                }
                if (fileCursor != null) {
                    fileCursor.close();
                }
            }
            return path;
        }
        return null;
    }
}

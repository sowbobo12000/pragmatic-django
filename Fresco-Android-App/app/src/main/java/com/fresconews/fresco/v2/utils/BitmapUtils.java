package com.fresconews.fresco.v2.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.ragnarok.rxcamera.RxCameraData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.internal.Util;

/**
 * Created by wumau on 9/27/2016.
 */
public class BitmapUtils {
    public static BitmapDescriptor getBitmapDescriptor(Context context, int id) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, id);
        int h = context.getResources().getDimensionPixelSize(R.dimen.assignment_marker_size);
        int w = context.getResources().getDimensionPixelSize(R.dimen.assignment_marker_size);
        vectorDrawable.setBounds(0, 0, w, h);
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bm);
    }

    public static Bitmap rotateBitmap(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();

            m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);

                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            }
            catch (OutOfMemoryError ex) {
                ex.printStackTrace();
            }
        }
        return b;
    }

    @Nullable
    public static Bitmap rotateBitmapIfNecessary(String picturePath, RxCameraData cameraData) throws IOException {
        Bitmap normalBitmap = null;
        Bitmap rotatedBitmap = null;
        int orientation = 0;

        // Find out what the orientation of the bitmap is
        ExifInterface ei = new ExifInterface(picturePath);
        orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        // Rotate the bitmap depending on the orientation
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            LogUtils.i("ImageViewAdapters", "needed to rotate the image"); // gets triggered more often than it should.
            //Consider the below alternate once returning to Bitmap rotation. Also consider just using camera orientation and matrix width.
            //Also consider .setRotationOptions(RotationOptions.autoRotate()) from Fresco library in ImageViewAdapters.

//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = 5;
//            normalBitmap = BitmapFactory.decodeByteArray(cameraData.cameraData, 0, cameraData.cameraData.length, options);

            normalBitmap = BitmapFactory.decodeByteArray(cameraData.cameraData, 0, cameraData.cameraData.length);
            if (normalBitmap != null) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBitmap = rotateBitmap(normalBitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBitmap = rotateBitmap(normalBitmap, 180);
                        break;
                }
            }
        }

        // Return the rotated bitmap, free up the old bitmap
        if (rotatedBitmap != null) {
            normalBitmap.recycle();
            normalBitmap = null;
            return rotatedBitmap;
        }
        LogUtils.i("ImageViewAdapters", "am not rotating the image");

        return normalBitmap;
    }

    public static Uri bitmapToUriConverter(Bitmap bitmap) {
        Uri uri = null;

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Fresco2.getContext().getPackageName());
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(mediaFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            uri = Uri.fromFile(mediaFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            Util.closeQuietly(out);
        }

        return uri;
    }

    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth;
        int height_tmp = o.outHeight;
        int scale = 1;

        while (true) {
            if (width_tmp < requiredSize || height_tmp < requiredSize) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }
}

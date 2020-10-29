package com.fresconews.fresco.framework.rx;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import com.fresconews.fresco.v2.utils.LogUtils;

import java.io.File;

import rx.Observable;

public class RxMediaScannerConnection {
    private static final String TAG = RxMediaScannerConnection.class.getSimpleName();

    public static Observable<Uri> scanFile(Context context, String fileName) {
        return Observable.create(subscriber -> {
            if (fileName == null) {
                subscriber.onError(new RuntimeException("Error saving picture"));
                return;
            }
            LogUtils.i(TAG, "Scanning Picture");
            MediaScannerConnection.scanFile(
                    context,
                    new String[]{fileName},
                    null,
                    (path, uri) -> {
                        LogUtils.i(TAG, "Added to MediaStore: " + uri.toString());
                        subscriber.onNext(uri);
                        subscriber.onCompleted();
                    });
        });
    }

    public static void deleteAndScanFile(final Context context, String fileName) {
        MediaScannerConnection.scanFile(
                context,
                new String[]{fileName},
                null,
                (path, uri) -> {
                    if (uri != null && path != null) {
                        File file = new File(path);
                        if (file.exists()) {
                            boolean b = file.delete();
                            if (b) {
                                LogUtils.d(TAG, "DELETED " + path);
                                context.getContentResolver().delete(uri, null, null);
                            }
                        }
                    }
                });
    }
}

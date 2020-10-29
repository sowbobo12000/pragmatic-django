package com.fresconews.fresco.framework.persistence.managers;

import android.content.Context;

import com.fresconews.fresco.framework.persistence.models.GalleryCreateRequest;
import com.fresconews.fresco.framework.persistence.models.UploadStatusMessage;

public class UploadManager {
    private static final String TAG = UploadManager.class.getSimpleName();

    private Context mContext;

    public UploadManager(Context context) {
        mContext = context;
    }

    public void create(GalleryCreateRequest createRequest) {
        FrescoUploadService2.startUpload(mContext, createRequest);
    }

    public void retryUpload() {
        FrescoUploadService2.retryUpload(mContext);
    }

    public UploadStatusMessage getCurrentUploadStatus() {
        return FrescoUploadService2.getCurrentUploadStatus();
    }
}

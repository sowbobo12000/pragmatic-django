package com.fresconews.fresco.framework.persistence.models;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by ryan on 7/14/2016.
 */
public class UploadStatusMessage {

    public static final int ERROR = -1;
    public static final int DONE = 0;
    public static final int UPLOADING = 1;

    private String errorMessage;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ERROR, DONE, UPLOADING})
    public @interface UploadStatus {
    }

    private
    @UploadStatus
    int mUploadStatus;
    private int mProgress; //Progress can only be 100 MAX

    public UploadStatusMessage(@UploadStatus int uploadStatus, int progress) {
        mUploadStatus = uploadStatus;
        mProgress = progress;
    }

    public UploadStatusMessage(@UploadStatus int uploadStatus, int progress, String errorMessage) {
        mUploadStatus = uploadStatus;
        mProgress = progress;
        this.errorMessage = errorMessage;
    }

    @UploadStatus
    public int getUploadStatus() {
        return mUploadStatus;
    }

    public void setUploadStatus(int uploadStatus) {
        mUploadStatus = uploadStatus;
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress = progress;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

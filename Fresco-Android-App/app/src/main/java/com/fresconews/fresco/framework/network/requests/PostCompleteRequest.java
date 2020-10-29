package com.fresconews.fresco.framework.network.requests;

import com.google.gson.annotations.SerializedName;

public class PostCompleteRequest {
    @SerializedName("key")
    private String key;

    @SerializedName("uploadId")
    private String uploadId;

    @SerializedName("eTags")
    private String[] eTags;

    //<editor-fold desc="Getters and Setters">
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String[] geteTags() {
        return eTags;
    }

    public void seteTags(String[] eTags) {
        this.eTags = eTags;
    }
    //</editor-fold>
}

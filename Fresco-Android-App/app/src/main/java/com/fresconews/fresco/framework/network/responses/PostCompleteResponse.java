package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

public class PostCompleteResponse {
    @SerializedName("Location")
    private String location;

    @SerializedName("Bucket")
    private String bucket;

    @SerializedName("Key")
    private String key;

    @SerializedName("ETag")
    private String eTag;

    //<editor-fold desc="Getters and Setters">
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }
    //</editor-fold>
}

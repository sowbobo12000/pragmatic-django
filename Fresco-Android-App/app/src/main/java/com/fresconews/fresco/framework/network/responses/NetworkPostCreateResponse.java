package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ryan on 8/5/2016.
 */
public class NetworkPostCreateResponse {
    @SerializedName("gallery_id")
    private String galleryId;

    @SerializedName("post_id")
    private String postId;

    @SerializedName("key")
    private String key;

    @SerializedName("uploadId")
    private String uploadId;

    @SerializedName("upload_urls")
    private String[] urls;

    @SerializedName("upload_url")
    private String url;

    //<editor-fold desc="Getters and Setters">
    public String getGalleryId() {
        return galleryId;
    }

    public void setGalleryId(String galleryId) {
        this.galleryId = galleryId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

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

    public String[] getUrls() {
        return urls;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    //Added in for single part
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    //</editor-fold>
}

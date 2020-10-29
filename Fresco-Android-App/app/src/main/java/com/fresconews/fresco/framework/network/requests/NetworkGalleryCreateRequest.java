package com.fresconews.fresco.framework.network.requests;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class NetworkGalleryCreateRequest {
    @SerializedName("caption")
    private String caption;

    @SerializedName("assignment_id")
    private String assignmentId;

    @SerializedName("outlet_id")
    private String outletId;

    @SerializedName("posts_new")
    private NetworkPostCreateRequest[] posts;

    //<editor-fold desc="Getters and Setters">
    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getOutletId() {
        return outletId;
    }

    public void setOutletId(String outletId) {
        this.outletId = outletId;
    }

    public NetworkPostCreateRequest[] getPosts() {
        return posts;
    }

    public void setPosts(NetworkPostCreateRequest[] posts) {
        this.posts = posts;
    }
    //</editor-fold>

    public static class NetworkPostCreateRequest {
        @SerializedName("lat")
        private double lat;

        @SerializedName("lng")
        private double lng;

        @SerializedName("address")
        private String address;

        @SerializedName("contentType")
        private String contentType;

        @SerializedName("fileSize")
        private Long fileSize; //setting to object so we can hopefully make it null to trigger singlepart ic_upload.

        @SerializedName("chunkSize")
        private Long chunkSize;

        @SerializedName("captured_at")
        private Date capturedAt;

        //<editor-fold desc="Getters and Setters">
        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public Long getFileSize() {
            return fileSize;
        }

        public void setFileSize(Long fileSize) {
            this.fileSize = fileSize;
        }

        public Long getChunkSize() {
            return chunkSize;
        }

        public void setChunkSize(Long chunkSize) {
            this.chunkSize = chunkSize;
        }

        public Date getCapturedAt() {
            return capturedAt;
        }

        public void setCapturedAt(Date capturedAt) {
            this.capturedAt = capturedAt;
        }

        //</editor-fold>
    }
}

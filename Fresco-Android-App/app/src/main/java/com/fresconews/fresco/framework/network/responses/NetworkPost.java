package com.fresconews.fresco.framework.network.responses;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Ryan on 5/31/2016.
 */
public class NetworkPost extends NetworkFrescoObject {
    @Nullable
    @SerializedName("stream")
    private String stream;

    @SerializedName("image")
    private String image;

    @SerializedName("byline")
    private String byline;

    @SerializedName("address")
    private String address;

    @SerializedName("width")
    private int width;

    @SerializedName("height")
    private int height;

    @SerializedName("status")
    private int status;

    @SerializedName("duration")
    private Integer duration;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("captured_at")
    private Date capturedAt;

    @SerializedName("parent_id")
    private String parentId;

    @SerializedName("parent")
    private NetworkPostParent networkPostParent;

    @SerializedName("purchases")
    private NetworkPurchase[] purchases;

    //<editor-fold desc="Getters and Setters">
    @Nullable
    public String getStream() {
        return stream;
    }

    public void setStream(@Nullable String stream) {
        this.stream = stream;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getByline() {
        return byline;
    }

    public void setByline(String byline) {
        this.byline = byline;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(Date capturedAt) {
        this.capturedAt = capturedAt;
    }

    public NetworkPostParent getNetworkPostParent() {
        return networkPostParent;
    }

    public void setNetworkPostParent(NetworkPostParent networkPostParent) {
        this.networkPostParent = networkPostParent;
    }

    public NetworkPurchase[] getPurchases() {
        return purchases;
    }

    public void setPurchases(NetworkPurchase[] purchases) {
        this.purchases = purchases;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    //</editor-fold>
}

package com.fresconews.fresco.framework.network.responses;

import com.github.filosganga.geogson.model.Point;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class NetworkAssignment {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("caption")
    private String caption;

    @SerializedName("status")
    private int status;

    @SerializedName("address")
    private String address;

    @SerializedName("starts_at")
    private Date startsAt;

    @SerializedName("ends_at")
    private Date endsAt;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    @SerializedName("outlets")
    private NetworkOutlet[] outlets;

    @SerializedName("error")
    private NetworkFrescoError error;

    @SerializedName("location")
    private Point location;

    @SerializedName("radius")
    private float radius;

    @SerializedName("accepted")
    private boolean accepted;

    @SerializedName("is_acceptable")
    private boolean isAcceptable;


    //<editor-fold desc="Getter and Setter">
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(Date startsAt) {
        this.startsAt = startsAt;
    }

    public Date getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Date endsAt) {
        this.endsAt = endsAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public NetworkOutlet[] getOutlets() {
        return outlets;
    }

    public void setOutlets(NetworkOutlet[] outlets) {
        this.outlets = outlets;
    }

    public NetworkFrescoError getError() {
        return error;
    }

    public void setError(NetworkFrescoError error) {
        this.error = error;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isAcceptable() {
        return isAcceptable;
    }

    public void setAcceptable(boolean acceptable) {
        isAcceptable = acceptable;
    }

    //</editor-fold>
}

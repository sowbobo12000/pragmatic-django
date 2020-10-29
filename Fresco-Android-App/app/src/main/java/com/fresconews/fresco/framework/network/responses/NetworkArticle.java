package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by ryan on 6/27/2016.
 */
public class NetworkArticle {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("outlet_id")
    private String outletId;

    @SerializedName("outlet")
    private NetworkOutlet outlet;

    @SerializedName("link")
    private String link;

    @SerializedName("favicon")
    private String favicon;

    @SerializedName("time_created")
    private Date timeCreated;

     //<editor-fold desc="Getters And Setters">
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

    public String getOutletId() {
        return outletId;
    }

    public void setOutletId(String outletId) {
        this.outletId = outletId;
    }

    public NetworkOutlet getOutlet() {
        return outlet;
    }

    public void setOutlet(NetworkOutlet outlet) {
        this.outlet = outlet;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Date timeCreated) {
        this.timeCreated = timeCreated;
    }
    //</editor-fold>
}

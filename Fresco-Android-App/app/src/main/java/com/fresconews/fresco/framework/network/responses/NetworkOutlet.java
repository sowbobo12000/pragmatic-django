package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ryan on 6/27/2016.
 */
public class NetworkOutlet {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("bio")
    private String bio;

    @SerializedName("link")
    private String link;

    @SerializedName("avatar")
    private String avatar;

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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    //</editor-fold>
}

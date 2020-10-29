package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by techjini on 12/7/16.
 */
public class NetworkStory extends NetworkFrescoObject {
    @SerializedName("title")
    private String title;

    @SerializedName("caption")
    private String caption;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    @SerializedName("action_at")
    private Date actionAt;

    @SerializedName("likes")
    private int likes;

    @SerializedName("reposts")
    private int reposts;

    @SerializedName("liked")
    private boolean liked;

    @SerializedName("address")
    private String address;

    @SerializedName("reposted")
    private boolean reposted;

    @SerializedName("reposted_by")
    private String repostedBy;

    @SerializedName("thumbnails")
    private NetworkStoryThumbnail[] thumbnails;

    public class NetworkStoryThumbnail {
        @SerializedName("height")
        private int height;

        @SerializedName("width")
        private int width;

        @SerializedName("image")
        private String image;

        //<editor-fold desc="Getters and Setters">
        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
        //</editor-fold>
    }

    //<editor-fold desc="Getters and Setters">
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

    public Date getActionAt() {
        return actionAt;
    }

    public void setActionAt(Date actionAt) {
        this.actionAt = actionAt;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getReposts() {
        return reposts;
    }

    public void setReposts(int reposts) {
        this.reposts = reposts;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isReposted() {
        return reposted;
    }

    public void setReposted(boolean reposted) {
        this.reposted = reposted;
    }

    public String getRepostedBy() {
        return repostedBy;
    }

    public void setRepostedBy(String repostedBy) {
        this.repostedBy = repostedBy;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public NetworkStoryThumbnail[] getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(NetworkStoryThumbnail[] thumbnails) {
        this.thumbnails = thumbnails;
    }
    //</editor-fold>
}

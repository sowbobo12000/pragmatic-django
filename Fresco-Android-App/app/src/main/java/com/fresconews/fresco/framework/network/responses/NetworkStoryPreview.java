package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

import java.net.URL;

/**
 * Created by ryan on 6/22/2016.
 */
public class NetworkStoryPreview extends NetworkFrescoObject {
    @SerializedName("likes")
    private int likes;

    @SerializedName("liked")
    private int liked;

    @SerializedName("reposts")
    private int reposts;

    @SerializedName("reposted")
    private int reposted;

    @SerializedName("title")
    private String title;

    @SerializedName("caption")
    private String caption;

    @SerializedName("galleries")
    private int galleries;

    @SerializedName("thumbnails")
    private NetworkStoryThumbnail[] thumbnails;

    //<editor-fold desc="Getters and Setters">
    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getLiked() {
        return liked;
    }

    public void setLiked(int liked) {
        this.liked = liked;
    }

    public int getReposts() {
        return reposts;
    }

    public void setReposts(int reposts) {
        this.reposts = reposts;
    }

    public int getReposted() {
        return reposted;
    }

    public void setReposted(int reposted) {
        this.reposted = reposted;
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

    public int getGalleries() {
        return galleries;
    }

    public void setGalleries(int galleries) {
        this.galleries = galleries;
    }

    public NetworkStoryThumbnail[] getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(NetworkStoryThumbnail[] thumbnails) {
        this.thumbnails = thumbnails;
    }
    //</editor-fold>

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
}

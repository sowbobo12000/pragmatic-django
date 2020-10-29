package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by ryan on 6/22/2016.
 */
public class NetworkFeedObject extends NetworkFrescoObject {
    //<editor-fold desc="Story">
    @SerializedName("title")
    private String title;

    @SerializedName("galleries")
    private int galleries;
    //</editor-fold>

    //<editor-fold desc="Gallery">
    @SerializedName("posts")
    private NetworkPost[] posts;

    @SerializedName("tags")
    private String[] tags;

    @SerializedName("rating")
    private int rating;
    //</editor-fold>

    //<editor-fold desc="Both">
    @SerializedName("caption")
    private String caption;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    @SerializedName("likes")
    private int likes;

    @SerializedName("reposts")
    private int reposts;

    @SerializedName("sources")
    private NetworkFrescoSource[] sources;

    @SerializedName("action_at")
    private Date actionAt;
    //</editor-fold>

    public class NetworkFrescoSource {
        @SerializedName("source")
        private String source;

        @SerializedName("user_id")
        private String userId;

        @SerializedName("username")
        private String username;

        //<editor-fold desc="Getters and Setters">
        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
        //</editor-fold>
    }
}

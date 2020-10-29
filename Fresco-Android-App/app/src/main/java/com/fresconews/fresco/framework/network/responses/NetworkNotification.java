package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by wumau on 9/14/2016.
 */

public class NetworkNotification {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("type")
    private String type;

    @SerializedName("title")
    private String title;

    @SerializedName("body")
    private String body;

    @SerializedName("seen")
    private boolean seen;

    @SerializedName("meta")
    private Meta meta;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("ends_at")
    private Date endsAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public String getAssignmentId() {
        return getMeta().assignmentId;
    }

    public String getStoryId() {
        return getMeta().storyId;
    }

    public String getGalleryId() {
        return getMeta().galleryId;
    }

    public String getCommentId() {
        return getMeta().commentId;
    }

    public String getOutletId() {
        return getMeta().outletId;
    }

    public String getPostId() {
        return getMeta().postId;
    }

    public String[] getUserIds() {
        return getMeta().userIds;
    }

    public String[] getGalleryIds() {
        return getMeta().galleryIds;
    }

    public String[] getCommentIds() {
        return getMeta().commentIds;
    }

    public String[] getStoryIds() {
        return getMeta().storyIds;
    }

    public String[] getPostIds() {
        return getMeta().postIds;
    }

    public boolean isGlobal() {
        return getMeta().global;
    }

    public String getImage() {
        return getMeta().image;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Date endsAt) {
        this.endsAt = endsAt;
    }

    class Meta {
        @SerializedName("image")
        private String image;

        @SerializedName("assignment_id")
        private String assignmentId;

        @SerializedName("story_id")
        private String storyId;

        @SerializedName("gallery_id")
        private String galleryId;

        @SerializedName("comment_id")
        private String commentId;

        @SerializedName("outlet_id")
        private String outletId;

        @SerializedName("post_id")
        private String postId;

        @SerializedName("user_ids")
        private String[] userIds;

        @SerializedName("gallery_ids")
        private String[] galleryIds;

        @SerializedName("comment_ids")
        private String[] commentIds;

        @SerializedName("story_ids")
        private String[] storyIds;

        @SerializedName("post_ids")
        private String[] postIds;

        @SerializedName("is_global")
        private boolean global;
    }
}


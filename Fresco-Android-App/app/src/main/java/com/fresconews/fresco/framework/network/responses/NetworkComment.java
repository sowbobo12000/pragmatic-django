package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Blaze on 8/25/2016.
 */
public class NetworkComment {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("comment")
    private String comment;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    @SerializedName("user")
    private NetworkUser user;

    @SerializedName("position")
    private int position;

    @SerializedName("entities")
    private NetworkCommentEntity[] networkCommentEntities;


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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public NetworkUser getUser() {
        return user;
    }

    public void setUser(NetworkUser user) {
        this.user = user;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public NetworkCommentEntity[] getNetworkCommentEntities() {
        return networkCommentEntities;
    }

    public void setNetworkCommentEntities(NetworkCommentEntity[] networkCommentEntities) {
        this.networkCommentEntities = networkCommentEntities;
    }
}

package com.fresconews.fresco.framework.persistence.models;

import com.fresconews.fresco.framework.network.responses.NetworkComment;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;

/**
 * Created by Blaze on 8/25/2016.
 */

@Table(database = FrescoDatabase.class)
public class Comment extends BaseModel {

    @PrimaryKey
    private String id;

    @Column
    private String userId;

    @Column
    private String comment;

    @Column
    private Date createdAt;

    @Column
    private Date updatedAt;

    @Column
    private String username;

    @Column
    private String avaterUrl;

    @Column
    private String galleryId;

    @Column
    private int position;

    public static Comment from(NetworkComment networkComment) {
        Comment comment = new Comment();
        comment.setCommentFrom(networkComment);
        return comment;
    }

    public void setCommentFrom(NetworkComment networkComment) {
        id = networkComment.getId();
        userId = networkComment.getUserId();
        comment = networkComment.getComment();
        createdAt = networkComment.getCreatedAt();
        updatedAt = networkComment.getUpdatedAt();
        position = networkComment.getPosition();
        if (networkComment.getUser() != null) {
            username = networkComment.getUser().getUsername();
            avaterUrl = networkComment.getUser().getAvatar();
        }
    }

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvaterUrl() {
        return avaterUrl;
    }

    public void setAvaterUrl(String avaterUrl) {
        this.avaterUrl = avaterUrl;
    }

    public String getGalleryId() {
        return galleryId;
    }

    public void setGalleryId(String galleryId) {
        this.galleryId = galleryId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
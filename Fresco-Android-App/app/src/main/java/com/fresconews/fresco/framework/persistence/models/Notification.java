package com.fresconews.fresco.framework.persistence.models;

import com.fresconews.fresco.framework.network.responses.NetworkNotification;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;

/**
 * Created by Maurice on 31/08/2016.
 */
@Table(database = FrescoDatabase.class)
public class Notification extends BaseModel {

    @PrimaryKey
    private String id;

    @Column
    private String type;

    @Column
    private String body;

    @Column
    private String title;

    @Column
    private String userId;

    @Column
    private String assignmentId;

    @Column
    private String storyId;

    @Column
    private String galleryId;

    @Column
    private String commentId;

    @Column
    private String outletId;

    @Column
    private String postId;

    @Column
    private String userIds;

    @Column
    private String galleryIds;

    @Column
    private String commentIds;

    @Column
    private String storyIds;

    @Column
    private String postIds;

    @Column
    private String image;

    @Column
    private boolean global;

    @Column
    private boolean seen;

    @Column
    private Date createdAt;

    @Column
    private Date endsAt;

    public static Notification from(NetworkNotification networkNotification) {
        Notification notification = new Notification();
        notification.id = networkNotification.getId();
        notification.type = networkNotification.getType();
        notification.userId = networkNotification.getUserId();
        notification.body = networkNotification.getBody();
        notification.title = networkNotification.getTitle();
        notification.seen = networkNotification.isSeen();
        notification.galleryId = networkNotification.getGalleryId();
        notification.assignmentId = networkNotification.getAssignmentId();
        notification.commentId = networkNotification.getCommentId();
        notification.storyId = networkNotification.getStoryId();
        notification.outletId = networkNotification.getOutletId();
        notification.postId = networkNotification.getPostId();
        notification.userIds = stringArrayToString(networkNotification.getUserIds());
        notification.galleryIds = stringArrayToString(networkNotification.getGalleryIds());
        notification.commentIds = stringArrayToString(networkNotification.getCommentIds());
        notification.storyIds = stringArrayToString(networkNotification.getStoryIds());
        notification.postIds = stringArrayToString(networkNotification.getPostIds());
        notification.global = networkNotification.isGlobal();
        notification.image = networkNotification.getImage();
        notification.endsAt = networkNotification.getEndsAt();

        return notification;
    }

    public static String stringArrayToString(String[] stringArray) {
        String result = "";
        if (stringArray != null) {
            for (String string : stringArray) {
                result += string + ",";
            }
            if (!result.isEmpty()) {
                result = result.substring(0, result.length() - 1);
            }
        }
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGalleryId() {
        return galleryId;
    }

    public void setGalleryId(String galleryId) {
        this.galleryId = galleryId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getOutletId() {
        return outletId;
    }

    public void setOutletId(String outletId) {
        this.outletId = outletId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserIds() {
        return userIds;
    }

    public void setUserIds(String userIds) {
        this.userIds = userIds;
    }

    public String getGalleryIds() {
        return galleryIds;
    }

    public void setGalleryIds(String galleryIds) {
        this.galleryIds = galleryIds;
    }

    public String getCommentIds() {
        return commentIds;
    }

    public void setCommentIds(String commentIds) {
        this.commentIds = commentIds;
    }

    public String getStoryIds() {
        return storyIds;
    }

    public void setStoryIds(String storyIds) {
        this.storyIds = storyIds;
    }

    public String getPostIds() {
        return postIds;
    }

    public void setPostIds(String postIds) {
        this.postIds = postIds;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
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

    @Override
    public int hashCode() {
        return getId().hashCode() + getType().hashCode() + (isSeen() ? 1 : 0);
    }

    @Override
    public boolean equals(Object other) {
        if ((other == null) || (getClass() != other.getClass())) {
            return false;
        }

        Notification otherNotification = (Notification) other;
        if (getId() != null && otherNotification.getId() != null && !getId().equals(otherNotification.getId())) {
            return false;
        }
        if (getType() != null && otherNotification.getType() != null && !getType().equals(otherNotification.getType())) {
            return false;
        }
        if (isSeen() != otherNotification.isSeen()) {
            return false;
        }

        return true;
    }
}

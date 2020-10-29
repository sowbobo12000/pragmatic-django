package com.fresconews.fresco.framework.persistence.models;

import com.fresconews.fresco.framework.network.responses.NetworkCommentEntity;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by Blaze on 9/26/2016.
 */

@Table(database = FrescoDatabase.class)
public class CommentEntity extends BaseModel {

    @PrimaryKey
    private String id;

    @Column
    private String entityId;

    @Column
    private String commentId;

    @Column
    private int startIndex;

    @Column
    private int endIndex;

    @Column
    private String entityType;

    @Column
    private String text;

    public static CommentEntity from(NetworkCommentEntity networkCommentEntity) {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.id = Long.toString(System.nanoTime());
        commentEntity.entityId = networkCommentEntity.getEntityId();
        commentEntity.commentId = networkCommentEntity.getCommentId();
        commentEntity.startIndex = networkCommentEntity.getStartIndex();
        commentEntity.endIndex = networkCommentEntity.getEndIndex();
        commentEntity.entityType = networkCommentEntity.getEntityType();
        commentEntity.text = networkCommentEntity.getText();

        return commentEntity;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

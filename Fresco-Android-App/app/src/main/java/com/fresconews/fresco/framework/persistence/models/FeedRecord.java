package com.fresconews.fresco.framework.persistence.models;

import android.support.annotation.StringDef;

import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;

/**
 * Created by ryan on 8/10/2016.
 */
@Table(database = FrescoDatabase.class)
public class FeedRecord extends BaseModel {

    public static final String GALLERY = "gallery";
    public static final String STORY = "story";

    @StringDef({GALLERY, STORY})
    public @interface RecordType {}

    @PrimaryKey
    private String id;

    @Column
    private String feedId;

    @Column
    @RecordType
    private String type;

    @Column
    private String itemId;

    @Column
    private Date actionAt;

    public static FeedRecord from(Gallery gallery, String feedId) {
        FeedRecord result = new FeedRecord();

        result.feedId = feedId;
        result.type = GALLERY;
        result.itemId = gallery.getId();
        result.actionAt = gallery.getActionAt();

        result.id = result.feedId + ":" + result.itemId;

        return result;
    }

    public static FeedRecord from(Story story, String feedId) {
        FeedRecord result = new FeedRecord();

        result.feedId = feedId;
        result.type = STORY;
        result.itemId = story.getId();
        result.actionAt = story.getActionAt();

        result.id = result.feedId + ":" + result.itemId;

        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Date getActionAt() {
        return actionAt;
    }

    public void setActionAt(Date actionAt) {
        this.actionAt = actionAt;
    }
}

package com.fresconews.fresco.framework.persistence.models;

import com.fresconews.fresco.framework.network.responses.NetworkStory;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

@Table(database = FrescoDatabase.class)
public class StoryThumbnail extends BaseModel {
    @PrimaryKey(autoincrement = true)
    private int id;

    @Column
    private int width;

    @Column
    private int height;

    @Column
    private String image;

    @ForeignKey
    private ForeignKeyContainer<Story> story;

    public void associateStory(Story story) {
        this.story = FlowManager.getContainerAdapter(Story.class).toForeignKeyContainer(story);
    }

    public static StoryThumbnail from(Story story, NetworkStory.NetworkStoryThumbnail thumbnail) {
        StoryThumbnail storyThumbnail = new StoryThumbnail();

        storyThumbnail.height = thumbnail.getHeight();
        storyThumbnail.width = thumbnail.getWidth();
        storyThumbnail.image = thumbnail.getImage();

        storyThumbnail.associateStory(story);

        return storyThumbnail;
    }

    //<editor-fold desc="Getters and Setters">
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ForeignKeyContainer<Story> getStory() {
        return story;
    }

    public void setStory(ForeignKeyContainer<Story> story) {
        this.story = story;
    }

    //</editor-fold>
}

package com.fresconews.fresco.framework.persistence.models;

import com.fresconews.fresco.framework.network.responses.NetworkArticle;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import java.util.Date;

@Table(database = FrescoDatabase.class)
public class Article extends BaseModel {
    @PrimaryKey
    private String id;

    @Column
    private String title;

    @Column
    private String link;

    @Column
    private String favicon;

    @Column
    private Date timeCreated;

    @ForeignKey
    private ForeignKeyContainer<Gallery> parentGallery;

    public void associateGallery(Gallery parentGallery) {
        this.parentGallery = FlowManager.getContainerAdapter(Gallery.class).toForeignKeyContainer(parentGallery);
    }

    public static Article from(NetworkArticle networkArticle, Gallery parentGallery) {
        Article article = new Article();

        article.id = networkArticle.getId();
        article.title = networkArticle.getTitle();
        article.link = networkArticle.getLink();
        article.favicon = networkArticle.getFavicon();
        article.timeCreated = networkArticle.getTimeCreated();

        article.associateGallery(parentGallery);

        return article;
    }

    //<editor-fold desc="Getters And Setters">
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Date timeCreated) {
        this.timeCreated = timeCreated;
    }

    public ForeignKeyContainer<Gallery> getParentGallery() {
        return parentGallery;
    }

    public void setParentGallery(ForeignKeyContainer<Gallery> parentGallery) {
        this.parentGallery = parentGallery;
    }

    //</editor-fold>
}

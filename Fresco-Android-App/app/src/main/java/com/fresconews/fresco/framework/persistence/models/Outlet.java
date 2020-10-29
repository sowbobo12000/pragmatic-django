package com.fresconews.fresco.framework.persistence.models;

import com.fresconews.fresco.framework.network.responses.NetworkOutlet;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(database = FrescoDatabase.class)
public class Outlet extends BaseModel {
    @PrimaryKey
    private String id;

    @Column
    private String title;

    @Column
    private String bio;

    @Column
    private String link;

    @Column
    private String avatar;

    public static Outlet from(NetworkOutlet networkOutlet) {
        Outlet outlet = new Outlet();

        outlet.id = networkOutlet.getId();

        outlet.title = networkOutlet.getTitle();
        outlet.bio = networkOutlet.getBio();
        outlet.link = networkOutlet.getLink();
        outlet.avatar = networkOutlet.getAvatar();

        return outlet;
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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    //</editor-fold>
}

package com.fresconews.fresco.framework.persistence.models;

import android.support.annotation.Nullable;

import com.fresconews.fresco.framework.network.responses.NetworkPost;
import com.fresconews.fresco.framework.network.responses.NetworkPurchase;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ryan on 6/3/2016.
 */
@ModelContainer
@Table(database = FrescoDatabase.class)
public class Post extends BaseModel {
    @PrimaryKey
    private String id;

    @Column
    private String byline;

    @Column
    private String address;

    @Column
    private int width;

    @Column
    private int height;

    @Column
    private Date createdAt;

    @Column
    private Date capturedAt;

    @Column
    @Nullable
    private String stream;

    @Column
    private String image;

    @Column
    private int status;

    @ForeignKey
    private ForeignKeyContainer<User> owner;

    @Column
    private String external_account_name;

    @Column
    private String external_account_username;

    @Column
    private String external_url;

    @Column
    private String external_source;

    private List<Purchase> purchases;

    @Column
    private int index;

    @Column
    private String parentId;

    @Column
    private int duration;

    @OneToMany(methods = {OneToMany.Method.LOAD, OneToMany.Method.SAVE}, variableName = "purchases", isVariablePrivate = true)

    public List<Purchase> loadPurchases() {
        if (purchases == null || purchases.isEmpty()) {
            purchases = SQLite.select()
                              .from(Purchase.class)
                              .where(Purchase_Table.parentPost_id.eq(id))
                              .queryList();
        }
        return purchases;
    }

    private void associateOwner(User owner) {
        this.owner = FlowManager.getContainerAdapter(User.class).toForeignKeyContainer(owner);
    }

    public Post() {
        purchases = new ArrayList<>();
    }

    public static Post from(NetworkPost networkPost, int index) {

        Post post = new Post();

        post.id = networkPost.getId();
        post.index = index;

        post.byline = networkPost.getByline();

        post.width = networkPost.getWidth();
        post.height = networkPost.getHeight();

        post.createdAt = networkPost.getCreatedAt();
        post.capturedAt = networkPost.getCapturedAt();

        post.image = networkPost.getImage();
        post.stream = networkPost.getStream();

        post.address = networkPost.getAddress();

        post.status = networkPost.getStatus();

        post.parentId = networkPost.getParentId();
        if (networkPost.getDuration() == null) {
            post.duration = -1;
        }
        else {
            post.duration = networkPost.getDuration();
        }

        if (networkPost.getOwner() != null) {
            User owner = User.from(networkPost.getOwner());
            post.associateOwner(owner);
            if (!owner.exists()) {
                owner.save();
            }
        }
        else {
            post.owner = new ForeignKeyContainer<>(User.class);
        }

        if (networkPost.getNetworkPostParent() != null) {
            post.external_account_name = networkPost.getNetworkPostParent().getExternal_account_name();
            post.external_url = networkPost.getNetworkPostParent().getExternal_url();
            post.external_source = networkPost.getNetworkPostParent().getExternal_source(); //twitter
            post.external_account_username = networkPost.getNetworkPostParent().getExternal_account_username();
        }

        NetworkPurchase[] purchases = networkPost.getPurchases();
        if (purchases != null) {
            for (NetworkPurchase networkPurchase : purchases) {
                Purchase purchase = Purchase.from(post, networkPurchase);
                purchase.save();
                post.purchases.add(purchase);
            }
        }

        return post;
    }

    //<editor-fold desc="Getters and Setters">
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getByline() {
        return byline;
    }

    public void setByline(String byline) {
        this.byline = byline;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(Date capturedAt) {
        this.capturedAt = capturedAt;
    }

    @Nullable
    public String getStream() {
        return stream;
    }

    public void setStream(@Nullable String stream) {
        this.stream = stream;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ForeignKeyContainer<User> getOwner() {
        return owner;
    }

    public void setOwner(ForeignKeyContainer<User> owner) {
        this.owner = owner;
    }

    public String getExternal_account_name() {
        return external_account_name;
    }

    public void setExternal_account_name(String external_account_name) {
        this.external_account_name = external_account_name;
    }

    public String getExternal_url() {
        return external_url;
    }

    public void setExternal_url(String external_url) {
        this.external_url = external_url;
    }

    public String getExternal_source() {
        return external_source;
    }

    public void setExternal_source(String external_source) {
        this.external_source = external_source;
    }

    public String getExternal_account_username() {
        return external_account_username;
    }

    public void setExternal_account_username(String external_account_username) {
        this.external_account_username = external_account_username;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    //</editor-fold>
}

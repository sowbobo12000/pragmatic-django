package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Blaze on 9/20/2016.
 * This is what contains info about the twitter/social info
 */
public class NetworkPostParent {

    @SerializedName("address")
    private String address;

    @SerializedName("caption")
    private String caption;

    @SerializedName("external_account_id")
    private String external_account_id;

    @SerializedName("external_account_name")
    private String external_account_name;

    @SerializedName("external_account_username")
    private String external_account_username;

    @SerializedName("external_id")
    private String external_id;

    @SerializedName("external_source")
    private String external_source;

    @SerializedName("external_url")
    private String external_url;

    @SerializedName("id")
    private String id;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getExternal_account_id() {
        return external_account_id;
    }

    public void setExternal_account_id(String external_account_id) {
        this.external_account_id = external_account_id;
    }

    public String getExternal_account_name() {
        return external_account_name;
    }

    public void setExternal_account_name(String external_account_name) {
        this.external_account_name = external_account_name;
    }

    public String getExternal_id() {
        return external_id;
    }

    public void setExternal_id(String external_id) {
        this.external_id = external_id;
    }

    public String getExternal_source() {
        return external_source;
    }

    public void setExternal_source(String external_source) {
        this.external_source = external_source;
    }

    public String getExternal_url() {
        return external_url;
    }

    public void setExternal_url(String external_url) {
        this.external_url = external_url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExternal_account_username() {
        return external_account_username;
    }

    public void setExternal_account_username(String external_account_username) {
        this.external_account_username = external_account_username;
    }
}

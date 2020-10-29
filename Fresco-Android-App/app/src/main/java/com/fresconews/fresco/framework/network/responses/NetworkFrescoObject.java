package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ryan on 5/31/2016.
 */
public class NetworkFrescoObject {

    @SerializedName("id")
    private String id;

    @SerializedName("object")
    private String object;

    @SerializedName("owner")
    private NetworkFrescoUser owner;

    @SerializedName("error")
    private NetworkFrescoError error;

    //<editor-fold desc="Getters and Setters">
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public NetworkFrescoUser getOwner() {
        return owner;
    }

    public void setOwner(NetworkFrescoUser owner) {
        this.owner = owner;
    }

    public NetworkFrescoError getError() {
        return error;
    }

    public void setError(NetworkFrescoError error) {
        this.error = error;
    }
    //</editor-fold>

}

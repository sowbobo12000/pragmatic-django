package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

public class NetworkAssignmentFindResult {
    @SerializedName("nearby")
    private NetworkAssignment[] nearby;

    @SerializedName("global")
    private NetworkAssignment[] global;

    @SerializedName("error")
    private NetworkFrescoError error;

    //<editor-fold desc="Getters and Setters">
    public NetworkAssignment[] getNearby() {
        return nearby;
    }

    public void setNearby(NetworkAssignment[] nearby) {
        this.nearby = nearby;
    }

    public NetworkAssignment[] getGlobal() {
        return global;
    }

    public void setGlobal(NetworkAssignment[] global) {
        this.global = global;
    }

    public NetworkFrescoError getError() {
        return error;
    }

    public void setError(NetworkFrescoError error) {
        this.error = error;
    }
    //</editor-fold>
}

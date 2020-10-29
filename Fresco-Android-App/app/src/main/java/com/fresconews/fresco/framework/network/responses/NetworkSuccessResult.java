package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ryan on 8/1/2016.
 */
public class NetworkSuccessResult {
    @SerializedName("success")
    private String success;

    @SerializedName("error")
    private NetworkFrescoError error;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public NetworkFrescoError getError() {
        return error;
    }

    public void setError(NetworkFrescoError error) {
        this.error = error;
    }
}

package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by wumau on 12/15/2016.
 */
public class NetworkReport {
    @SerializedName("reason")
    private String reason;

    @SerializedName("message")
    private String message;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("id")
    private String id;

    @SerializedName("status")
    private int status;
}

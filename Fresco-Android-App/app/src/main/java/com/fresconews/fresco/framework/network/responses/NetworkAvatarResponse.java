package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

import java.net.URI;

/**
 * Created by ryan on 6/24/2016.
 */
public class NetworkAvatarResponse {
    @SerializedName("avatar")
    private URI avatar;

    public URI getAvatar() {
        return avatar;
    }

    public void setAvatar(URI avatar) {
        this.avatar = avatar;
    }
}

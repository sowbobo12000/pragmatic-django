package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Blaze on 1/18/2017.
 */

public class NetworkAccessToken {

    @SerializedName("token")
    private String token;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("expiresIn")
    private long expiresInMillis;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresInMillis() {
        return expiresInMillis;
    }

    public void setExpiresInMillis(long expiresInMillis) {
        this.expiresInMillis = expiresInMillis;
    }
}

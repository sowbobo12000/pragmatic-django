package com.fresconews.fresco.framework.network.requests;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Blaze on 1/18/2017.
 */

public class NetworkAuthRequest {
    @SerializedName("grant_type")
    private String grantType;

    @SerializedName("scope")
    private String scope;

    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("token")
    private String oldBearerToken;

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getOldBearerToken() {
        return oldBearerToken;
    }

    public void setOldBearerToken(String oldBearerToken) {
        this.oldBearerToken = oldBearerToken;
    }
}

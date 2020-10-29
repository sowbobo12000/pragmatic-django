package com.fresconews.fresco.framework.persistence.models;

import com.fresconews.fresco.framework.persistence.managers.SegmentManager;

/**
 * Created by ryan on 6/13/2016.
 */
public class Session {
    private String token;

    private String clientToken;
    private String clientRefreshToken;

    private String userToken;
    private String userRefreshToken;

    private String username;
    private String userId;
    private int pastVersionCode;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getPastVersionCode() {
        return pastVersionCode;
    }

    public void setPastVersionCode(int pastVersionCode) {
        this.pastVersionCode = pastVersionCode;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getClientRefreshToken() {
        return clientRefreshToken;
    }

    public void setClientRefreshToken(String clientRefreshToken) {
        this.clientRefreshToken = clientRefreshToken;
    }

    public String getUserRefreshToken() {
        return userRefreshToken;
    }

    public void setUserRefreshToken(String userRefreshToken) {
        this.userRefreshToken = userRefreshToken;
    }
}

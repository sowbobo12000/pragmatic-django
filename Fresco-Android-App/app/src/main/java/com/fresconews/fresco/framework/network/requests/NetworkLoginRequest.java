package com.fresconews.fresco.framework.network.requests;

import android.support.annotation.StringDef;

import com.google.gson.annotations.SerializedName;

public class NetworkLoginRequest {

    public static final String TWITTER = "twitter";
    public static final String FACEBOOK = "facebook";
    public static final String GOOGLE = "google";

    @StringDef({TWITTER, FACEBOOK, GOOGLE})
    public @interface SocialPlatform {
    }

    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("token")
    private String token;

    @SerializedName("jwt")
    private String jwt;

    @SerializedName("secret")
    private String secret;

    @SerializedName("platform")
    private
    @SocialPlatform
    String platform;

    @SerializedName("installation")
    private NetworkInstallation installation;

    //<editor-fold desc="Getters and Setters">
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

    public NetworkInstallation getInstallation() {
        return installation;
    }

    public void setInstallation(NetworkInstallation installation) {
        this.installation = installation;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public
    @SocialPlatform
    String getPlatform() {
        return platform;
    }

    public void setPlatform(@SocialPlatform String platform) {
        this.platform = platform;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    //</editor-fold>
}

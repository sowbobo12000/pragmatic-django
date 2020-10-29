package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ryan on 6/13/2016.
 */
public class NetworkAuthResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("error")
    private NetworkFrescoError error;

    @SerializedName("terms")
    private NetworkTerms terms;

    @SerializedName("valid_password")
    private boolean validPassword;

    @SerializedName("expiresIn")
    private long expiresInMillis;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public NetworkFrescoError getError() {
        return error;
    }

    public void setError(NetworkFrescoError error) {
        this.error = error;
    }

    public NetworkTerms getTerms() {
        return terms;
    }

    public void setTerms(NetworkTerms terms) {
        this.terms = terms;
    }

    public boolean isValidPassword() {
        return validPassword;
    }

    public void setValidPassword(boolean validPassword) {
        this.validPassword = validPassword;
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

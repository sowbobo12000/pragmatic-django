package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Blaze on 1/18/2017.
 */

public class NetworkAuthTokenResponse {

    @SerializedName("access_token")
    private NetworkAuthResponse networkAuthResponse; //token information is stupidly and unnecessarily nested

    @SerializedName("token_type")
    private String tokenType;

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public NetworkAuthResponse getNetworkAuthResponse() {
        return networkAuthResponse;
    }

    public void setNetworkAuthResponse(NetworkAuthResponse networkAuthResponse) {
        this.networkAuthResponse = networkAuthResponse;
    }
}

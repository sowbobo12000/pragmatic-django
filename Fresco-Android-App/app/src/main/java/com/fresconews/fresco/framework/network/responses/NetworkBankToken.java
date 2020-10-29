package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Blaze on 8/22/2016.
 */
public class NetworkBankToken {

    @SerializedName("id")
    private String tokenId;

    @SerializedName("bank_account")
    private NetworkBankAccount networkBankAccount;

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public NetworkBankAccount getBankAccount() {
        return networkBankAccount;
    }

    public void setNetworkBankAccount(NetworkBankAccount networkBankAccount) {
        this.networkBankAccount = networkBankAccount;
    }

}

package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by wumau on 12/13/2016.
 */
public class NetworkPurchase {

    @SerializedName("id")
    private String id;

    @SerializedName("amount")
    private int amount;

    @SerializedName("outlet")
    private NetworkOutlet outlet;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NetworkOutlet getOutlet() {
        return outlet;
    }

    public void setOutlet(NetworkOutlet outlet) {
        this.outlet = outlet;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

}

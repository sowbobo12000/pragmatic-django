package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Blaze on 8/22/2016.
 */
public class NetworkBankAccount {

    @SerializedName("id")
    private String id;

    @SerializedName("object")
    private String object;

    @SerializedName("account_holder_name")
    private String account_holder_name;

    @SerializedName("account_holder_type")
    private String account_holder_type;

    @SerializedName("bank_name")
    private String bank_name;

    @SerializedName("country")
    private String country;

    @SerializedName("currency")
    private String currency;

    @SerializedName("last4")
    private String last4;

    @SerializedName("routing_number")
    private String routing_number;

    @SerializedName("status")
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getAccount_holder_name() {
        return account_holder_name;
    }

    public void setAccount_holder_name(String account_holder_name) {
        this.account_holder_name = account_holder_name;
    }

    public String getAccount_holder_type() {
        return account_holder_type;
    }

    public void setAccount_holder_type(String account_holder_type) {
        this.account_holder_type = account_holder_type;
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLast4() {
        return last4;
    }

    public void setLast4(String last4) {
        this.last4 = last4;
    }

    public String getRouting_number() {
        return routing_number;
    }

    public void setRouting_number(String routing_number) {
        this.routing_number = routing_number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

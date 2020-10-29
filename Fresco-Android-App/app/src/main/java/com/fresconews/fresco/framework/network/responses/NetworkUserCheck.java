package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Blaze on 11/9/2016.
 */

public class NetworkUserCheck {

//    "available": false,
//    "fields_unavailable": ["username"]

    @SerializedName("available")
    private boolean available;

    @SerializedName("fields_unavailable")
    private String[] fieldsUnavailable;

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String[] getFieldsUnavailable() {
        return fieldsUnavailable;
    }

    public void setFieldsUnavailable(String[] fieldsUnavailable) {
        this.fieldsUnavailable = fieldsUnavailable;
    }
}

package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Blaze on 10/5/2016.
 *
 * Used for the Identification Document
 */

public class NetworkStripeFile {

//    {
//        "id": "file_18wzPK2eZvKYlo2CKdXyu53D",
//            "object": "file_upload",
//            "created": 1474659146,
//            "purpose": "identity_document",
//            "size": 76545,
//            "type": "jpg"
//    }

    @SerializedName("id")
    private String id;

    @SerializedName("object")
    private String object;

    @SerializedName("purpose")
    private String purpose;

    @SerializedName("type")
    private String type;

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

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

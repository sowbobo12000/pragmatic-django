package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ryan on 6/13/2016.
 */
public class NetworkFrescoError {
    @SerializedName("status")
    private int status;

    @SerializedName("type")
    private String type;

    @SerializedName(value="msg", alternate={"message"})
    private String msg;

    @SerializedName("param")
    private String param;

    @SerializedName("value")
    private String value;

    //<editor-fold desc="Getters and Setters">
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    //</editor-fold>
}

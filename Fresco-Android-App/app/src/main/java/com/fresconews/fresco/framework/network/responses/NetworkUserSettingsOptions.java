package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Blaze on 10/14/2016.
 */

public class NetworkUserSettingsOptions {

    @SerializedName("send_sms")
    private boolean sendSms;


    @SerializedName("send_push")
    private boolean sendPush;

    @SerializedName("send_email")
    private boolean sendEmail;

    @SerializedName("send_fresco")
    private boolean sendFresco;

    public boolean isSendSms() {
        return sendSms;
    }

    public void setSendSms(boolean sendSms) {
        this.sendSms = sendSms;
    }

    public boolean isSendPush() {
        return sendPush;
    }

    public void setSendPush(boolean sendPush) {
        this.sendPush = sendPush;
    }

    public boolean isSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public boolean isSendFresco() {
        return sendFresco;
    }

    public void setSendFresco(boolean sendFresco) {
        this.sendFresco = sendFresco;
    }
}

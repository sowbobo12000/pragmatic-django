package com.fresconews.fresco.framework.network.requests;

import android.provider.Settings;

import com.fresconews.fresco.BuildConfig;
import com.google.gson.annotations.SerializedName;

/**
 * Created by ryan on 6/23/2016.
 */
public class NetworkInstallation {
    @SerializedName("app_version")
    private String appVersion;

    @SerializedName("platform")
    private String platform;

    @SerializedName("device_token") //also double as new device token
    private String deviceToken;

    @SerializedName("old_device_token") //to be sent up once device token changes
    private String oldDeviceToken;

    @SerializedName("device_id")
    private String deviceId;

    public NetworkInstallation(String deviceToken, String deviceId) {
        appVersion = BuildConfig.VERSION_NAME;
        platform = "android";
        this.deviceToken = deviceToken;
        this.deviceId = deviceId;
    }

    public NetworkInstallation(String oldDeviceToken, String newDeviceToken, String deviceId) {
        appVersion = BuildConfig.VERSION_NAME;
        platform = "android";
        this.oldDeviceToken = oldDeviceToken;
        this.deviceToken = newDeviceToken;
        this.deviceId = deviceId;
    }

    //<editor-fold desc="Getters and Setters">
    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getOldDeviceToken() {
        return oldDeviceToken;
    }

    public void setOldDeviceToken(String oldDeviceToken) {
        this.oldDeviceToken = oldDeviceToken;
    }

    //</editor-fold>
}

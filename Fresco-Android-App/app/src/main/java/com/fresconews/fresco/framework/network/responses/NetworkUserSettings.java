package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Blaze on 10/14/2016.
 */

public class NetworkUserSettings {


    @SerializedName("type")
    private String type;

    @SerializedName("description")
    private String description;

    @SerializedName("title")
    private String title;

    @SerializedName("options")
    private NetworkUserSettingsOptions networkUserSettingsOptions;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public NetworkUserSettingsOptions getNetworkUserSettingsOptions() {
        return networkUserSettingsOptions;
    }

    public void setNetworkUserSettingsOptions(NetworkUserSettingsOptions networkUserSettingsOptions) {
        this.networkUserSettingsOptions = networkUserSettingsOptions;
    }
}

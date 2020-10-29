package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by wumau on 9/14/2016.
 */

public class NetworkNotifications extends NetworkFrescoObject {

    @SerializedName("unseen_count")
    private int unseenCount;

    @SerializedName("feed")
    private NetworkNotification[] notifications;

    public NetworkNotification[] getNotifications() {
        return notifications;
    }

    public int getUnseenCount() {
        return unseenCount;
    }

}

package com.fresconews.fresco.framework.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.NetworkUtils;

/**
 * Created by wumau on 10/10/2016.
 */

public class ConnectionReceiver extends BroadcastReceiver {
    private static final String TAG = ConnectionReceiver.class.getSimpleName();

    public static final String CONNECTION_RECEIVER_INTENT_FILTER = "CONNECTION_RECEIVER_INTENT_FILTER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (NetworkUtils.getConnectivityStatus(context) == NetworkUtils.TYPE_NOT_CONNECTED) {
            LogUtils.d(TAG, "Disconnected");
        }
        else {
            LogUtils.d(TAG, "Connected");
            Fresco2.startLocationService();
        }
    }
}

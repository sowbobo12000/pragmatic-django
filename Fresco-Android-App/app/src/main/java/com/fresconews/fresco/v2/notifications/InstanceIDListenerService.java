package com.fresconews.fresco.v2.notifications;

import com.fresconews.fresco.v2.utils.LogUtils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by ryan on 6/22/2016.
 */
public class InstanceIDListenerService extends FirebaseInstanceIdService {
    private static final String TAG = InstanceIDListenerService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        LogUtils.i(TAG, "Token: " + token);
    }
}

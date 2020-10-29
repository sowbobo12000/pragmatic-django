package com.fresconews.fresco.v2.backgroundservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.v2.utils.LogUtils;

import javax.inject.Inject;

public class OnClearFromRecentService extends Service {
    private static final String TAG = OnClearFromRecentService.class.getSimpleName();

    @Inject
    AnalyticsManager analyticsManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "Service Started");
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "Service Destroyed");
    }

    public void onTaskRemoved(Intent rootIntent) {
        //Code here
        if (analyticsManager != null) {
            analyticsManager.exitedOnboarding();
        }
        stopSelf();
    }
}
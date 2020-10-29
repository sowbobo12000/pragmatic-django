package com.fresconews.fresco.v2.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.persistence.managers.NotificationIntentManager;
import com.fresconews.fresco.v2.utils.DimensionUtils;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import io.smooch.core.Smooch;

public class FcmListenerService extends FirebaseMessagingService {
    private static final String TAG = FcmListenerService.class.getSimpleName();
    public static final String FCM_INTENT_FILTER = "FCM_INTENT_FILTER";

    @Inject
    NotificationIntentManager notificationIntentManager;

    @Override
    public void onCreate() {
        super.onCreate();
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);
        LogUtils.i(TAG, "GCM Listener Started");
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        LogUtils.i(TAG, "Push recieved: " + remoteMessage.getFrom());
        Map<String, String> data = remoteMessage.getData();
        boolean isSmoochNotification = false;

        for (String key : data.keySet()) {
            LogUtils.d(TAG, "Key: " + key + ": " + data.get(key));
            if (key.equals("smoochNotification")) {
                isSmoochNotification = true;
            }
        }

        String type = data.get(NotificationIntentManager.TYPE);
        String title = data.get(NotificationIntentManager.TITLE);
        boolean isCustomSmoochMessage = false;
        if (type != null && type.equals(NotificationType.NEWS_CUSTOM)) { //default custom push notif
            isCustomSmoochMessage = true;
            if (title == null) {
                isCustomSmoochMessage = false;
            }
            else if (title.equals("smooch-invite")) {
                Smooch.track("smooch-invite");
            }
            else if (title.equals("upload-error")) {
                Smooch.track("upload-error");
            }
            else if (title.equals("fresco-support")) {
                Smooch.track("fresco-support");
            }
            else {
                isCustomSmoochMessage = false;
            }
        }

        if (isSmoochNotification) {
            io.smooch.core.FcmService.triggerSmoochNotification(remoteMessage.getData(), this);
        }
        else {
            if (isCustomSmoochMessage) {
                return;
            }
            notificationIntentManager
                    .findIntent(this, data, false, false) //false means look for primary intent, not from notif feed
                    .onErrorReturn(throwable -> null)
                    .subscribe(mainIntent -> {
                        if (mainIntent == null) {
                            return;
                        }
                        PendingIntent pendingIntent = PendingIntent.getActivity(FcmListenerService.this, 0, mainIntent, PendingIntent.FLAG_ONE_SHOT);
                        notificationIntentManager
                                .findIntent(this, data, true, false) //true means look for secondary intent, false means not from notif feed
                                .onErrorReturn(throwable -> null)
                                .subscribe(secondaryIntent -> {
                                    if (secondaryIntent == null) {
                                        return;
                                    }
                                    PendingIntent secondaryPendingIntent = PendingIntent.getActivity(FcmListenerService.this, 0, secondaryIntent, PendingIntent.FLAG_ONE_SHOT);
                                    launchIntent(pendingIntent, secondaryPendingIntent, data);
                                });
                    });
        }
    }

    private void launchIntent(PendingIntent pendingIntent, PendingIntent secondaryPendingIntent, Map<String, String> data) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String body = data.get(NotificationIntentManager.BODY);
        String type = data.get(NotificationIntentManager.TYPE);
        String title = data.get(NotificationIntentManager.TITLE);
        String image = data.get(NotificationIntentManager.IMAGE);

        AsyncTask.execute(() -> {
            Bitmap largeIcon = null;
            if (!TextUtils.isEmpty(image)) {
                try {
                    int dim = DimensionUtils.convertDpToPixel(64f, FcmListenerService.this);
                    largeIcon = Glide.with(FcmListenerService.this).load(image).asBitmap().centerCrop().into(dim, dim).get();
                }
                catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(FcmListenerService.this)
                    .setSmallIcon(R.drawable.ic_fresco_white)
                    .setColor(ContextCompat.getColor(FcmListenerService.this, R.color.fresco_yellow))
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            LogUtils.i(TAG, "pending intent null - " + Boolean.toString(pendingIntent == null));

            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
            bigTextStyle.setBigContentTitle(title);
            bigTextStyle.bigText(body);
            notificationBuilder.setStyle(bigTextStyle);

            if (largeIcon != null) {
                notificationBuilder.setLargeIcon(largeIcon);
            }

            if (!TextUtils.isEmpty(type)) {
                switch (type) {
                    case NotificationType.DISPATCH_NEW_ASSIGNMENT:
                        if (!data.containsKey(NotificationIntentManager.IS_GLOBAL) ||
                                (data.containsKey(NotificationIntentManager.IS_GLOBAL) && !Boolean.valueOf(data.get(NotificationIntentManager.IS_GLOBAL)))) {
                            NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.directions, getString(R.string.directions), secondaryPendingIntent)
                                    .build();
                            notificationBuilder.addAction(action);
                        }
                        break;
                }
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(new Random().nextInt(Integer.MAX_VALUE - 1), notificationBuilder.build());

            Intent notificationReceived = new Intent(FCM_INTENT_FILTER);
            LocalBroadcastManager.getInstance(FcmListenerService.this).sendBroadcast(notificationReceived);
        });

    }
}

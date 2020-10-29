package com.fresconews.fresco.framework.persistence.managers;

import android.content.Context;

import com.fresconews.fresco.framework.network.responses.NetworkNotification;
import com.fresconews.fresco.framework.network.responses.NetworkNotifications;
import com.fresconews.fresco.framework.network.responses.NetworkSuccessResult;
import com.fresconews.fresco.framework.network.services.NotificationFeedService;
import com.fresconews.fresco.framework.persistence.DBFlowDataSource;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.fresconews.fresco.framework.persistence.models.Notification;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Maurice on 31/08/2016.
 */
public class NotificationFeedManager {

    private static final int DELAY_NOTIFICATION_COUNT = 90; // Poll every 90 seconds

    private NotificationFeedService notificationFeedService;
    private SessionManager sessionManager;
    private Context context;

    public NotificationFeedManager(NotificationFeedService notificationFeedService, SessionManager sessionManager, Context context) {
        this.notificationFeedService = notificationFeedService;
        this.sessionManager = sessionManager;
        this.context = context;
    }

    public Observable<List<Notification>> downloadNotifications() {
        return handleNotifications(notificationFeedService.get());
    }

    public Observable<List<Notification>> downloadNotifications(int limit, String last) {
        return handleNotifications(notificationFeedService.get(limit, last));
    }

    public Observable<List<Notification>> downloadNotifications(int limit) {
        return handleNotifications(notificationFeedService.get(limit));
    }

    public IDataSource<Notification> getNotificationFeedDataSource(FlowCursorList.OnCursorRefreshListener<Notification> refreshListener) {
        FlowQueryList<Notification> queryList = SQLite.select()
                                                      .from(Notification.class)
                                                      .flowQueryList();
        queryList.registerForContentChanges(context);
        if (refreshListener != null) {
            queryList.addOnCursorRefreshListener(refreshListener);
        }
        return new DBFlowDataSource<>(queryList, true);
    }

    public Observable<Integer> getUnseenNotificationsCount() {
        return notificationFeedService.get()
                                      .subscribeOn(Schedulers.io())
                                      .filter(aLong -> sessionManager.isLoggedIn())
                                      .map(NetworkNotifications::getUnseenCount)
                                      .onErrorReturn(throwable -> 0);
    }

    public Observable<Integer> pollUnseenNotificationsCount() {
        return Observable.interval(0, DELAY_NOTIFICATION_COUNT, TimeUnit.SECONDS, Schedulers.io())
                         .filter(aLong -> sessionManager.isLoggedIn())
                         .onErrorReturn(throwable -> null)
                         .flatMap(i -> notificationFeedService.get()
                                                              .map(NetworkNotifications::getUnseenCount)
                                                              .onErrorReturn(throwable -> 0));
    }

    public Observable<NetworkSuccessResult> seeNotifications(List<String> ids) {
        return notificationFeedService.see(ids);
    }

    private Observable<List<Notification>> handleNotifications(Observable<NetworkNotifications> response) {
        return response.map(networkNotifications -> {
            List<Notification> result = new ArrayList<>(networkNotifications.getNotifications().length);
            for (NetworkNotification networkNotification : networkNotifications.getNotifications()) {
                Notification notification = Notification.from(networkNotification);
                result.add(notification);
            }

            FastStoreModelTransaction<Notification> storeGallery =
                    FastStoreModelTransaction.saveBuilder(FlowManager.getModelAdapter(Notification.class))
                                             .addAll(result)
                                             .build();
            FlowManager.getDatabase(FrescoDatabase.class)
                       .executeTransaction(storeGallery);

            return result;
        });
    }

    public void clearNotifications() {
        SQLite.delete(Notification.class)
              .execute();
    }
}

package com.fresconews.fresco.framework.network.services;

import com.fresconews.fresco.framework.network.responses.NetworkNotifications;
import com.fresconews.fresco.framework.network.responses.NetworkSuccessResult;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Maurice on 31/08/2016.
 */
public interface NotificationFeedService {
    @GET("user/notifications")
    Observable<NetworkNotifications> get();

    @GET("user/notifications")
    Observable<NetworkNotifications> get(
            @Query("limit") int limit
    );

    @GET("user/notifications")
    Observable<NetworkNotifications> get(
            @Query("limit") int limit,
            @Query("last") String last
    );

    @FormUrlEncoded
    @POST("user/notifications/see")
    Observable<NetworkSuccessResult> see(@Field("notification_ids[]") List<String> ids);
}

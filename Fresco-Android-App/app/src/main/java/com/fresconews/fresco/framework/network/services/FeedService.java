package com.fresconews.fresco.framework.network.services;

import com.fresconews.fresco.framework.network.responses.NetworkFrescoObject;

import java.util.Date;
import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface FeedService {
    @GET("feeds/{id}/user")
    Observable<List<NetworkFrescoObject>> user(
            @Path("id") String userId,
            @Query("limit") int limit
    );

    @GET("feeds/{id}/user")
    Observable<List<NetworkFrescoObject>> user(
            @Path("id") String userId,
            @Query("limit") int limit,
            @Query("last") Date last
    );

    @GET("feeds/{id}/likes")
    Observable<List<NetworkFrescoObject>> likes(
            @Path("id") String userId,
            @Query("limit") int limit
    );

    @GET("feeds/{id}/likes")
    Observable<List<NetworkFrescoObject>> likes(
            @Path("id") String userId,
            @Query("limit") int limit,
            @Query("last") Date last
    );

    @GET("feeds/{id}/following")
    Observable<List<NetworkFrescoObject>> following(
            @Path("id") String userId,
            @Query("limit") int limit
    );

    @GET("feeds/{id}/following")
    Observable<List<NetworkFrescoObject>> following(
            @Path("id") String userId,
            @Query("limit") int limit,
            @Query("last") Date last
    );
}

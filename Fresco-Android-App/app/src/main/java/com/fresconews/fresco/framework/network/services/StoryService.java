package com.fresconews.fresco.framework.network.services;

import com.fresconews.fresco.framework.network.responses.NetworkGallery;
import com.fresconews.fresco.framework.network.responses.NetworkSuccessResult;
import com.fresconews.fresco.framework.network.responses.NetworkStory;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by ryan on 6/22/2016.
 */
public interface StoryService {
    @GET("story/recent")
    Observable<List<NetworkStory>> recent(
            @Query("limit") int limit
    );
    @GET("story/recent")
    Observable<List<NetworkStory>> recent(
            @Query("limit") int limit,
            @Query("last") String last
    );

    @GET("story/{id}")
    Observable<NetworkStory> get(@Path("id") String id);

    @GET("story/{id}/galleries")
    Observable<List<NetworkGallery>> galleries(
            @Path("id") String id,
            @Query("limit") int limit
    );

    @GET("story/{id}/galleries")
    Observable<List<NetworkGallery>> galleries(
            @Path("id") String id,
            @Query("limit") int limit,
            @Query("last") String last
    );

    @POST("story/{id}/like")
    Observable<NetworkSuccessResult> like(
            @Path("id") String id
    );

    @POST("story/{id}/unlike")
    Observable<NetworkSuccessResult> unlike(
            @Path("id") String id
    );

    @POST("story/{id}/repost")
    Observable<NetworkSuccessResult> repost(
            @Path("id") String id
    );

    @POST("story/{id}/unrepost")
    Observable<NetworkSuccessResult> unrepost(
            @Path("id") String id
    );
}

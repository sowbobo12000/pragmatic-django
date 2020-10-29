package com.fresconews.fresco.framework.network.services;

import com.fresconews.fresco.framework.network.requests.NetworkGalleryCreateRequest;
import com.fresconews.fresco.framework.network.responses.NetworkComment;
import com.fresconews.fresco.framework.network.responses.NetworkGallery;
import com.fresconews.fresco.framework.network.responses.NetworkReport;
import com.fresconews.fresco.framework.network.responses.NetworkPost;
import com.fresconews.fresco.framework.network.responses.NetworkSuccessResult;
import com.fresconews.fresco.framework.network.responses.NetworkUser;

import java.util.Date;
import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Ryan on 5/31/2016.
 */
public interface GalleryService {
    @GET("gallery/highlights")
    Observable<List<NetworkGallery>> highlights(
            @Query("limit") int limit,
            @Query("last") String last
    );

    @GET("gallery/highlights")
    Observable<List<NetworkGallery>> highlights(
            @Query("limit") int limit
    );

    @GET("gallery/{id}")
    Observable<NetworkGallery> get(
            @Path("id") String gallery
    );

    @GET("gallery/{id}")
    Observable<List<NetworkGallery>> getList(
            @Path("id") String galleryIds
    );

    @GET("feeds/{userId}/user")
    Observable<List<NetworkGallery>> list(
            @Path("userId") String userId,
            @Query("last") Date last,
            @Query("limit") int limit
    );

    @GET("feeds/{userId}/user")
    Observable<List<NetworkGallery>> list(
            @Path("userId") String userId,
            @Query("limit") int limit
    );
    @GET("feeds/{userId}/user")
    Observable<List<NetworkGallery>> list(
            @Path("userId") String userId
    );

    @POST("gallery/submit")
    Observable<NetworkGallery> create(
            @Body NetworkGalleryCreateRequest create
    );

    @POST("gallery/{id}/like")
    Observable<NetworkSuccessResult> like(
            @Path("id") String id
    );

    @POST("gallery/{id}/unlike")
    Observable<NetworkSuccessResult> unlike(
            @Path("id") String id
    );

    @POST("gallery/{id}/repost")
    Observable<NetworkSuccessResult> repost(
            @Path("id") String id
    );

    @POST("gallery/{id}/unrepost")
    Observable<NetworkSuccessResult> unrepost(
            @Path("id") String id
    );

    @FormUrlEncoded
    @POST("gallery/{id}/comment")
    Observable<NetworkComment> comment(
            @Path("id") String galleryId,
            @Field("comment") String comment
    );

    @FormUrlEncoded
    @POST("gallery/{id}/comment/delete")
    Observable<NetworkSuccessResult> deleteComment(
            @Path("id") String galleryId,
            @Field("comment_id") String commentId
    );
    @GET("gallery/{id}/comment/{commentId}")
    Observable<NetworkComment> getComment(
            @Path("id") String galleryId,
            @Path("commentId") String comment
    );
    @GET("gallery/{id}/comments")
    Observable<List<NetworkComment>> getComments(
            @Path("id") String galleryId,
            @Query("limit") int limit
    );
    @GET("gallery/{id}/comments")
    Observable<List<NetworkComment>> getComments(
            @Path("id") String galleryId,
            @Query("limit") int limit,
            @Query("last") String last
    );
    @GET("gallery/{id}/comments")
    Observable<List<NetworkComment>> getComments(
            @Path("id") String galleryId,
            @Query("limit") int limit,
            @Query("last") String last,
            @Query("direction") String direction
    );

    @GET("gallery/{id}/likes")
    Observable<List<NetworkUser>> usersLiked(
            @Path("id") String id,
            @Query("limit") int limit,
            @Query("last") String lastId
    );

    @GET("gallery/{id}/likes")
    Observable<List<NetworkUser>> usersLiked(
            @Path("id") String id,
            @Query("limit") int limit
    );

    @GET("gallery/{id}/reposts")
    Observable<List<NetworkUser>> usersReposted(
            @Path("id") String id,
            @Query("limit") int limit
    );

    @GET("gallery/{id}/reposts")
    Observable<List<NetworkUser>> usersReposted(
            @Path("id") String id,
            @Query("limit") int limit,
            @Query("last") String last
    );

    @FormUrlEncoded
    @POST("gallery/{id}/report")
    Observable<NetworkReport> report(
            @Path("id") String id,
            @Field("reason") String reason,
            @Field("message") String message
    );

    @GET("gallery/{id}/purchases")
    Observable<List<NetworkPost>> getPurchasedPosts(
            @Path("id") String gallery
    );
}

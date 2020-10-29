package com.fresconews.fresco.framework.network.services;

import com.fresconews.fresco.framework.network.responses.NetworkAvatarResponse;
import com.fresconews.fresco.framework.network.responses.NetworkReport;
import com.fresconews.fresco.framework.network.responses.NetworkSettings;
import com.fresconews.fresco.framework.network.responses.NetworkSuccessResult;
import com.fresconews.fresco.framework.network.responses.NetworkUser;
import com.fresconews.fresco.framework.network.responses.NetworkUserCheck;
import com.fresconews.fresco.framework.network.responses.NetworkUserSettings;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface UserService {
    @GET("user/me")
    Observable<NetworkUser> me();

    @GET("user/{id}")
    Observable<NetworkUser> get(
            @Path("id") String id
    );

    @GET("user/check")
    Observable<NetworkUserCheck> checkEmail(
            @Query("email") String email
    );

    @GET("user/check")
    Observable<NetworkUserCheck> checkUsername(
            @Query("username") String username
    );

    @GET("user/check")
    Observable<NetworkUserCheck> checkUsernameAndEmail(
            @Query("username") String username,
            @Query("email") String email
    );

    @GET("user/settings")
    Observable<NetworkSettings> getSettings();

    @POST("user/disable")
    void disableUser(@Field("password") String password);

    @FormUrlEncoded
    @POST("user/update")
    Observable<NetworkSettings> updateRadius(@Field("radius") double radius);

    @FormUrlEncoded
    @POST("user/settings/update")
    Observable<List<NetworkUserSettings>> updateAssignmentNotifications(
            //`fresco`, `sms`, `email` or `push`
            @Field("notify-user-dispatch-new-assignment[send_fresco]") boolean showFrescoNotifs,
            @Field("notify-user-dispatch-new-assignment[send_push]") boolean showPushNotifs,
            @Field("notify-user-dispatch-new-assignment[send_sms]") boolean showSMSNotifs,
            @Field("notify-user-dispatch-new-assignment[send_email]") boolean showEmailNotifs

    );

    @GET("user/settings")
    Observable<List<NetworkUserSettings>> getUserSettings();

    @FormUrlEncoded
    @POST("user/update")
    Observable<NetworkSettings> updateUsername(
            @Field("username") String username,
            @Field("verify_password") String password
    );

    @FormUrlEncoded
    @POST("user/update")
    Observable<NetworkSettings> updateEmail(
            @Field("email") String email,
            @Field("verify_password") String password
    );

    @FormUrlEncoded
    @POST("user/update")
    Observable<NetworkSettings> updatePassword(
            @Field("password") String newPassword,
            @Field("verify_password") String oldPassword
    );

    @FormUrlEncoded
    @POST("user/update")
    Observable<NetworkSuccessResult> updateUsernameEmail(
            @Field("username") String username,
            @Field("email") String email,
            @Field("verify_password") String password
    );

    @FormUrlEncoded
    @POST("user/update")
    Observable<NetworkSuccessResult> updateSocialUsernameEmail(
            @Field("username") String username,
            @Field("email") String email,
            @Field("password") String password,
            @Field("platform") String platform,
            @Field("token") String token,
            @Field("secret") String secret
    );

    @FormUrlEncoded
    @POST("user/social/disconnect")
    Observable<NetworkUser> disconnectSocial(
            @Field("platform") String platform
    );

    @FormUrlEncoded
    @POST("user/social/connect")
    Observable<NetworkUser> connectSocial(
            @Field("platform") String platform,
            @Field("token") String token,
            @Field("secret") String secret
    );

    @FormUrlEncoded
    @POST("user/social/connect")
    Observable<NetworkUser> connectSocial(
            @Field("platform") String platform,
            @Field("token") String token
    );

    @FormUrlEncoded
    @POST("user/social/connect")
    Observable<NetworkUser> connectSocialGoogle(
            @Field("platform") String platform,
            @Field("jwt") String token
    );

    @FormUrlEncoded
    @POST("user/update")
    Observable<NetworkUser> updateProfile(
            @Field("full_name") String fullname,
            @Field("bio") String bio,
            @Field("location") String location
    );

    @Multipart
    @POST("user/avatar")
    Observable<NetworkAvatarResponse> updateAvatar(@Part MultipartBody.Part file);

    @POST("user/{id}/follow")
    Observable<NetworkSuccessResult> follow(@Path("id") String id);

    @POST("user/{id}/unfollow")
    Observable<NetworkSuccessResult> unfollow(@Path("id") String id);

    @GET("user/{id}/following")
    Observable<List<NetworkUser>> following(@Path("id") String id);

    @GET("user/{id}/following")
    Observable<List<NetworkUser>> following(
            @Path("id") String id,
            @Query("limit") int limit,
            @Query("last") String lastId
    );

    @GET("user/{id}/following")
    Observable<List<NetworkUser>> following(
            @Path("id") String id,
            @Query("limit") int limit
    );

    @GET("user/{id}/followers")
    Observable<List<NetworkUser>> followers(@Path("id") String id);

    @GET("user/{id}/followers")
    Observable<List<NetworkUser>> followers(
            @Path("id") String id,
            @Query("limit") int limit
    );

    @GET("user/{id}/followers")
    Observable<List<NetworkUser>> followers(
            @Path("id") String id,
            @Query("limit") int limit,
            @Query("last") String last
    );

    @GET("user/suggestions")
    Observable<List<NetworkUser>> suggestions();

    @POST("terms/accept")
    Observable<NetworkUser> acceptTOS();

    @FormUrlEncoded
    @POST("user/locate")
    Observable<NetworkSuccessResult> location(
            @Field("lng") float lng,
            @Field("lat") float lat
    );

    @FormUrlEncoded
    @POST("user/{id}/report")
    Observable<NetworkReport> report(
            @Path("id") String id,
            @Field("reason") String reason,
            @Field("message") String message
    );

    @POST("user/{id}/block")
    Observable<NetworkSuccessResult> block(
            @Path("id") String id
    );

    @POST("user/{id}/unblock")
    Observable<NetworkSuccessResult> unblock(
            @Path("id") String id
    );
}

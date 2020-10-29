package com.fresconews.fresco.framework.network.services;

import com.fresconews.fresco.framework.network.requests.NetworkAuthRequest;
import com.fresconews.fresco.framework.network.requests.NetworkLoginRequest;
import com.fresconews.fresco.framework.network.requests.NetworkSignupRequest;
import com.fresconews.fresco.framework.network.responses.NetworkAuthResponse;
import com.fresconews.fresco.framework.network.responses.NetworkAuthTokenResponse;
import com.fresconews.fresco.framework.network.responses.NetworkTerms;
import com.fresconews.fresco.framework.network.responses.NetworkUser;
import com.fresconews.fresco.framework.persistence.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

public interface AuthService {

    @POST("auth/signin/social")
    Observable<NetworkAuthResponse> signInSocial(@Body NetworkLoginRequest request);

    @POST("user/create")
    Observable<User> userCreate(@Body NetworkSignupRequest request);

    @FormUrlEncoded
    @POST("auth/checkSocial")
    Observable<Void> checkSocial(
            @Field("platform") String platform,
            @Field("token") String token,
            @Field("secret") String secret,
            @Field("jwt") String authCode);

    @POST("auth/signout")
    Observable<NetworkAuthResponse> signOut();

    @POST("user/update")
    Observable<NetworkUser> updateInstallation(@Body NetworkLoginRequest request);

    @POST("auth/token")
    Call<NetworkAuthTokenResponse> refreshToken(@Body NetworkAuthRequest request);

    @POST("auth/token/migrate")
    Call<NetworkAuthResponse> updateToken(@Body NetworkAuthRequest request); //only use this on update install of android app

    @POST("auth/token")
    Observable<NetworkAuthTokenResponse> generateClientToken(@Body NetworkAuthRequest request);

    @POST("auth/token")
    Observable<NetworkAuthTokenResponse> signInNewAuth(@Body NetworkAuthRequest request);

    @DELETE("auth/token")
    Observable<NetworkAuthTokenResponse> deleteUserToken();

    @GET("terms")
    Observable<NetworkTerms> terms();

}

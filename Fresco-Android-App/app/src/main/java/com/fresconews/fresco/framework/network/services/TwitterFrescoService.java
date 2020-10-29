package com.fresconews.fresco.framework.network.services;

import com.fresconews.fresco.framework.network.requests.PostCompleteRequest;
import com.fresconews.fresco.framework.network.responses.PostCompleteResponse;
import com.twitter.sdk.android.core.models.User;

import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;

import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Blaze on 10/13/2016.
 *
 * Told it's not a priority right now.
 */

public interface TwitterFrescoService {




    @GET("users/show")
    Observable<User> getUserById(
            @Query("user_id") String userId
    );

    @GET("users/show")
    Observable<User> getUserBy(
            @Query("user_id") String userId
    );


}

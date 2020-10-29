package com.fresconews.fresco.framework.network.services;

import com.fresconews.fresco.framework.network.requests.PostCompleteRequest;
import com.fresconews.fresco.framework.network.responses.PostCompleteResponse;

import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Url;
import rx.Observable;

public interface PostService {
    @POST("post/complete")
    Observable<PostCompleteResponse> complete(
            @Body PostCompleteRequest request
    );

    @PUT
    Observable<Response<Void>> upload(
            @Url String url,
            @Body RequestBody body
    );


    //Do we need a different ic_upload function for this

}

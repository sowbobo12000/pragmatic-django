package com.fresconews.fresco.framework.network.services;

import com.fresconews.fresco.framework.network.responses.NetworkAssignment;
import com.fresconews.fresco.framework.network.responses.NetworkAssignmentFindResult;
import com.fresconews.fresco.framework.persistence.models.Assignment;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import rx.Observable;

public interface AssignmentService {
    @GET("assignment/{id}")
    Observable<NetworkAssignment> get(@Path("id") String id);

    @GET("assignment/find")
    Observable<NetworkAssignmentFindResult> find(@QueryMap Map<String, String> find);

    @POST("assignment/{id}/accept")
    Observable<NetworkAssignment> accept(@Path("id") String id);

    @POST("assignment/{id}/unaccept")
    Observable<NetworkAssignment> unaccept(@Path("id") String id);

    @GET("assignment/accepted")
    Observable<NetworkAssignment> accepted();
}

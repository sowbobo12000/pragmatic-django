package com.fresconews.fresco.framework.network.services;

import com.fresconews.fresco.framework.network.responses.NetworkSearchResults;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Blaze on 7/22/2016.
 */
public interface SearchService {

    // STORY SEARCHES
    @GET("search/")
    Observable<NetworkSearchResults> searchStories(
            @Query("stories[q]") String query
    );

    @GET("search/")
    Observable<NetworkSearchResults> searchStories(
            @Query("stories[q]") String query,
            @Query("stories[limit]") int limit
    );

    @GET("search/")
    Observable<NetworkSearchResults> searchStories(
            @Query("stories[q]") String query,
            @Query("stories[limit]") int limit,
            @Query("stories[last]") String last
    );

    // GALLERY SEARCHES
    @GET("search/")
    Observable<NetworkSearchResults> searchGalleries(
            @Query("galleries[q]") String query
    );

    @GET("search/")
    Observable<NetworkSearchResults> searchGalleries(
            @Query("galleries[q]") String query,
            @Query("galleries[limit]") int limit
    );

    @GET("search/")
    Observable<NetworkSearchResults> searchGalleries(
            @Query("galleries[q]") String query,
            @Query("galleries[limit]") int limit,
            @Query("galleries[last]") String last

    );

    // USER SEARCHES
    @GET("search/")
    Observable<NetworkSearchResults> searchUsers(
            @Query("users[q]") String query
    );

    @GET("search/")
    Observable<NetworkSearchResults> searchUsers(
            @Query("users[q]") String query,
            @Query("users[limit]") int limit
    );

    @GET("search/")
    Observable<NetworkSearchResults> searchUsers(
            @Query("users[q]") String query,
            @Query("users[limit]") int limit,
            @Query("users[last]") String last
    );

    // USER AUTO COMPLETE
    @GET("search/")
    Observable<NetworkSearchResults> autoCompleteUsers(
            @Query("users[a][username]") String query
    );
}

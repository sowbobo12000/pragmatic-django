package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Blaze on 7/22/2016.
 * <p>
 * Explanation on structure.
 * For each type returned by search (NetworkUser, NetworkGallery, NetworkStory) each response has
 * a count and an actual List of Results for each type. This class is generic and handles all 3.
 */
public class NetworkSearchResultsType<T> extends NetworkFrescoObject {

    @SerializedName("count")
    private int count;

    @SerializedName("results")
    List<T> results;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }
}

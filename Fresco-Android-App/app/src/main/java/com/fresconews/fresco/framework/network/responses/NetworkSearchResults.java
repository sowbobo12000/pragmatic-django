package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Blaze on 7/25/2016.
 * <p>
 * Explanation on structure.
 * Search Service returns this type.
 * Depending on the query (if queried individually for galleries or stories or users) others fields
 * may be null.
 * This class then gives access to each NetworkSearchResultType.
 */
public class NetworkSearchResults {

    @SerializedName("storiesCount")
    private int storiesCount;

    @SerializedName("usersCount")
    private int usersCount;

    @SerializedName("galleries")
    NetworkSearchResultsType<NetworkGallery> galleries;

    @SerializedName("stories")
    NetworkSearchResultsType<NetworkStory> stories;

    @SerializedName("users")
    NetworkSearchResultsType<NetworkUser> users;

    public void setGalleries(NetworkSearchResultsType<NetworkGallery> galleries) {
        this.galleries = galleries;
    }

    public NetworkSearchResultsType<NetworkGallery> getGalleries() {
        return galleries;
    }

    public void setStories(NetworkSearchResultsType<NetworkStory> stories) {
        this.stories = stories;
    }

    public NetworkSearchResultsType<NetworkStory> getStories() {
        return stories;
    }

    public void setUsers(NetworkSearchResultsType<NetworkUser> users) {
        this.users = users;
    }

    public NetworkSearchResultsType<NetworkUser> getUsers() {
        return users;
    }

    public int getStoriesCount() {
        return stories.getCount();
    }

    public void setStoriesCount(int storiesCount) {
        stories.setCount(storiesCount);
        this.storiesCount = storiesCount;
    }

    public int getUsersCount() {
        return users.getCount();
    }

    public void setUsersCount(int usersCount) {
        users.setCount(usersCount);
        this.usersCount = usersCount;
    }

}

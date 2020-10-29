package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Ryan on 5/31/2016.
 */
public class NetworkGallery extends NetworkFrescoObject {
    @SerializedName("caption")
    private String caption;

    @SerializedName("tags")
    private String[] tags;

    @SerializedName("photo_count")
    private int photoCount;

    @SerializedName("video_count")
    private int videoCount;

    @SerializedName("rating")
    private int rating;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    @SerializedName("action_at")
    private Date actionAt;

    @SerializedName("highlighted_at")
    private Date highlightedAt;

    @SerializedName("likes")
    private int likes;

    @SerializedName("liked")
    private boolean liked;

    @SerializedName("reposts")
    private int reposts;

    @SerializedName("reposted")
    private boolean reposted;

    @SerializedName("reposted_by")
    private String repostedBy;

    @SerializedName("posts")
    private NetworkPost[] posts;

    @SerializedName("articles")
    private NetworkArticle[] articles;

    @SerializedName("stories")
    private NetworkStory[] stories;

    @SerializedName("posts_new")
    private NetworkPostCreateResponse[] newPosts;

    @SerializedName("comments")
    private int commentCount;

    @SerializedName("owner_id")
    private String ownerId;

    @SerializedName("external_account_id")
    private String externalAccountId;

    @SerializedName("importer_id")
    private String importerId;

    @SerializedName("curator_id")
    private String curatorId;

    //<editor-fold desc="Getters and Setters">
    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getActionAt() {
        return actionAt;
    }

    public void setActionAt(Date actionAt) {
        this.actionAt = actionAt;
    }

    public Date getHighlightedAt() {
        return highlightedAt;
    }

    public void setHighlightedAt(Date highlightedAt) {
        this.highlightedAt = highlightedAt;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public int getReposts() {
        return reposts;
    }

    public void setReposts(int reposts) {
        this.reposts = reposts;
    }

    public boolean isReposted() {
        return reposted;
    }

    public void setReposted(boolean reposted) {
        this.reposted = reposted;
    }

    public String getRepostedBy() {
        return repostedBy;
    }

    public void setRepostedBy(String repostedBy) {
        this.repostedBy = repostedBy;
    }

    public NetworkPost[] getPosts() {
        return posts;
    }

    public void setPosts(NetworkPost[] posts) {
        this.posts = posts;
    }

    public NetworkArticle[] getArticles() {
        return articles;
    }

    public void setArticles(NetworkArticle[] articles) {
        this.articles = articles;
    }

    public NetworkStory[] getStories() {
        return stories;
    }

    public void setStories(NetworkStory[] stories) {
        this.stories = stories;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }

    public NetworkPostCreateResponse[] getNewPosts() {
        return newPosts;
    }

    public void setNewPosts(NetworkPostCreateResponse[] newPosts) {
        this.newPosts = newPosts;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getExternalAccountId() {
        return externalAccountId;
    }

    public void setExternalAccountId(String externalAccountId) {
        this.externalAccountId = externalAccountId;
    }

    public String getImporterId() {
        return importerId;
    }

    public void setImporterId(String importerId) {
        this.importerId = importerId;
    }

    public String getCuratorId() {
        return curatorId;
    }

    public void setCuratorId(String curatorId) {
        this.curatorId = curatorId;
    }

    //</editor-fold>
}

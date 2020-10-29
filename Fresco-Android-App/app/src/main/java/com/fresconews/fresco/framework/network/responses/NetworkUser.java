package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ryan on 6/13/2016.
 */
public class NetworkUser {
    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("username")
    private String username;

    @SerializedName("bio")
    private String bio;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("followed_count")
    private int followedCount;

    @SerializedName("following_count")
    private int followingCount;

    @SerializedName("following")
    private boolean following;

    @SerializedName("location")
    private String location;

    @SerializedName("radius")
    private String radius;

    @SerializedName("due_by")
    private Long dueBy;

    @SerializedName("suspended_until")
    private String suspendedUntil;

    @SerializedName("blocked")
    private boolean blocked;

    @SerializedName("blocking")
    private boolean blocking;

    @SerializedName("disabled")
    private boolean disabled;

    @SerializedName("social_links")
    private NetworkSocialLinks socialLinks;

    @SerializedName("identity")
    private NetworkIdentity networkIdentity;

    @SerializedName("terms")
    private NetworkTerms terms;

    //<editor-fold desc="Getters and Setters">
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getFollowedCount() {
        return followedCount;
    }

    public void setFollowedCount(int followedCount) {
        this.followedCount = followedCount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public Long getDueBy() {
        return dueBy;
    }

    public void setDueBy(Long dueBy) {
        this.dueBy = dueBy;
    }

    public String getSuspendedUntil() {
        return suspendedUntil;
    }

    public void setSuspendedUntil(String suspendedUntil) {
        this.suspendedUntil = suspendedUntil;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public NetworkSocialLinks getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(NetworkSocialLinks socialLinks) {
        this.socialLinks = socialLinks;
    }

    public NetworkTerms getTerms() {
        return terms;
    }

    public void setTerms(NetworkTerms terms) {
        this.terms = terms;
    }

    public NetworkIdentity getNetworkIdentity() {
        return networkIdentity;
    }

    public void setNetworkIdentity(NetworkIdentity networkIdentity) {
        this.networkIdentity = networkIdentity;
    }

    //</editor-fold>
}

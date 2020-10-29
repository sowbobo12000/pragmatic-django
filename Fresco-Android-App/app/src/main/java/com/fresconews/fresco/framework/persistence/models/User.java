package com.fresconews.fresco.framework.persistence.models;

import com.fresconews.fresco.framework.network.responses.NetworkFrescoUser;
import com.fresconews.fresco.framework.network.responses.NetworkUser;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ManyToMany;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@ModelContainer
@Table(database = FrescoDatabase.class)
@ManyToMany(referencedTable = SearchQuery.class)
public class User extends BaseModel {
    @PrimaryKey
    private String id;

    @Column
    private String email;

    @Column
    private String username;

    @Column
    private String bio;

    @Column
    private String avatar;

    @Column
    private String fullName;

    @Column
    private int followedCount;

    @Column
    private int followingCount;

    @Column
    private boolean following;

    @Column
    private String location;

    @Column
    private String radius;

    @Column
    private boolean validPassword;

    @Column
    private int searchInt;

    @Column
    private Long dueBy;

    @Column
    private String facebookSocialLink;

    @Column
    private String twitterSocialLink;

    @Column
    private String googleSocialLink;

    @Column
    private String fieldsNeeded;

    @Column
    private String suspendedUntil;

    @Column
    private boolean blocked;

    @Column
    private boolean blocking;

    @Column
    private boolean disabled;

    @Column
    private boolean termsValid;

    @Column
    private String termsBodyText;

    @Column
    private Integer totalUsersFromSearchQuery;

    public static User from(NetworkUser networkUser) {
        User user = new User();

        user.id = networkUser.getId();
        user.email = networkUser.getEmail();
        user.username = networkUser.getUsername();
        user.bio = networkUser.getBio();
        user.avatar = networkUser.getAvatar();
        user.fullName = networkUser.getFullName();
        user.followedCount = networkUser.getFollowedCount();
        user.followingCount = networkUser.getFollowingCount();
        user.following = networkUser.isFollowing();
        user.location = networkUser.getLocation();
        user.radius = networkUser.getRadius();
        user.searchInt = -1;
        user.dueBy = networkUser.getDueBy();
        user.suspendedUntil = networkUser.getSuspendedUntil();
        user.blocked = networkUser.isBlocked();
        user.blocking = networkUser.isBlocking();
        user.disabled = networkUser.isDisabled();

        if (networkUser.getSocialLinks() != null) {
            user.twitterSocialLink = networkUser.getSocialLinks().getTwitter();
            user.facebookSocialLink = networkUser.getSocialLinks().getFacebook();
            user.googleSocialLink = networkUser.getSocialLinks().getGoogle();
        }

        if (networkUser.getTerms() != null) {
            user.termsValid = networkUser.getTerms().isValid();
            if (networkUser.getTerms().getTerms() != null) {
                user.termsBodyText = networkUser.getTerms().getTerms().replace('�', '"');
            }
        }

        return user;
    }

    public static User from(NetworkFrescoUser networkFrescoUser) {
        User user = new User();

        user.id = networkFrescoUser.getId();
        user.fullName = networkFrescoUser.getFullName();
        user.username = networkFrescoUser.getUsername();
        user.bio = networkFrescoUser.getBio();
        user.avatar = networkFrescoUser.getAvatar();
        user.location = networkFrescoUser.getLocation();
        user.following = false;
        user.searchInt = -1;

        return user;
    }

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

    public int getSearchInt() {
        return searchInt;
    }

    public void setSearchInt(int searchInt) {
        this.searchInt = searchInt;
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

    public String getFacebookSocialLink() {
        return facebookSocialLink;
    }

    public void setFacebookSocialLink(String facebookSocialLink) {
        this.facebookSocialLink = facebookSocialLink;
    }

    public String getTwitterSocialLink() {
        return twitterSocialLink;
    }

    public void setTwitterSocialLink(String twitterSocialLink) {
        this.twitterSocialLink = twitterSocialLink;
    }

    public String getGoogleSocialLink() {
        return googleSocialLink;
    }

    public void setGoogleSocialLink(String googleSocialLink) {
        this.googleSocialLink = googleSocialLink;
    }

    public String getFieldsNeeded() {
        return fieldsNeeded;
    }

    public void setFieldsNeeded(String fieldsNeeded) {
        this.fieldsNeeded = fieldsNeeded;
    }

    public boolean isValidPassword() {
        return validPassword;
    }

    public void setValidPassword(boolean validPassword) {
        this.validPassword = validPassword;
    }

    public boolean isTermsValid() {
        return termsValid;
    }

    public void setTermsValid(boolean termsValid) {
        this.termsValid = termsValid;
    }

    public String getTermsBodyText() {
        return termsBodyText;
    }

    public void setTermsBodyText(String termsBodyText) {
        this.termsBodyText = termsBodyText;
    }

    public Integer getTotalUsersFromSearchQuery() {
        return totalUsersFromSearchQuery;
    }

    public void setTotalUsersFromSearchQuery(Integer totalUsersFromSearchQuery) {
        this.totalUsersFromSearchQuery = totalUsersFromSearchQuery;
    }

    public String getSuspendedUntil() {
        return suspendedUntil;
    }

    public void setSuspendedUntil(String suspendedUntil) {
        this.suspendedUntil = suspendedUntil;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public int hashCode() {
        return getId().hashCode() + getUsername().hashCode() + getAvatar().hashCode() + getFullName().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        User otherUser = (User) other;
        if (getId() != null && otherUser.getId() != null && !getId().equals(otherUser.getId())) {
            return false;
        }
        if (getUsername() != null && otherUser.getUsername() != null && !getUsername().equals(otherUser.getUsername())) {
            return false;
        }
        if (getAvatar() != null && otherUser.getAvatar() != null && !getAvatar().equals(otherUser.getAvatar())) {
            return false;
        }
        if (getFullName() != null && otherUser.getFullName() != null && !getFullName().equals(otherUser.getFullName())) {
            return false;
        }

        return true;
    }

}


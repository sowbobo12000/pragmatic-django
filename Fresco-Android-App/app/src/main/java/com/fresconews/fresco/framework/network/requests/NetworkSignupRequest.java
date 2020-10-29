package com.fresconews.fresco.framework.network.requests;

import com.facebook.AccessToken;
import com.google.gson.annotations.SerializedName;
import com.twitter.sdk.android.core.TwitterAuthToken;

public class NetworkSignupRequest {
    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("full_name")
    private String fullname;

    @SerializedName("bio")
    private String bio;

    @SerializedName("location")
    private String location;

    @SerializedName("social_links")
    private SocialLinks socialLinks;

    @SerializedName("installation")
    private NetworkInstallation installation;

    public static NetworkSignupRequest fromTwitter(TwitterAuthToken authToken) {
        NetworkSignupRequest request = new NetworkSignupRequest();

        request.socialLinks = new SocialLinks();
        request.socialLinks.twitter = new Twitter();
        request.socialLinks.twitter.token = authToken.token;
        request.socialLinks.twitter.secret = authToken.secret;

        return request;
    }

    public static NetworkSignupRequest fromFacebook(AccessToken accessToken) {
        NetworkSignupRequest request = new NetworkSignupRequest();

        request.socialLinks = new SocialLinks();
        request.socialLinks.facebook = new Facebook();
        request.socialLinks.facebook.setToken(accessToken.getToken());

        return request;
    }

    public static NetworkSignupRequest fromGoogle(String authCode) {
        NetworkSignupRequest request = new NetworkSignupRequest();

        request.socialLinks = new SocialLinks();
        request.socialLinks.google = new Google();
        request.socialLinks.google.setAuthCode(authCode);

        return request;
    }

    //<editor-fold desc="Getters and Setters">
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public SocialLinks getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(SocialLinks socialLinks) {
        this.socialLinks = socialLinks;
    }

    public NetworkInstallation getInstallation() {
        return installation;
    }

    public void setInstallation(NetworkInstallation installation) {
        this.installation = installation;
    }
    //</editor-fold>

    public static class SocialLinks {
        @SerializedName("twitter")
        private Twitter twitter;

        @SerializedName("facebook")
        private Facebook facebook;

        @SerializedName("google")
        private Google google;

        //<editor-fold desc="Getters and Setters">
        public Twitter getTwitter() {
            return twitter;
        }

        public void setTwitter(Twitter twitter) {
            this.twitter = twitter;
        }

        public Facebook getFacebook() {
            return facebook;
        }

        public void setFacebook(Facebook facebook) {
            this.facebook = facebook;
        }

        public Google getGoogle() {
            return google;
        }

        public void setGoogle(Google google) {
            this.google = google;
        }
        //</editor-fold>
    }

    public static class Twitter {
        @SerializedName("token")
        private String token;

        @SerializedName("secret")
        private String secret;

        //<editor-fold desc="Getters and Setters">
        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
        //</editor-fold>
    }

    public static class Facebook {
        @SerializedName("token")
        private String token;

        //<editor-fold desc="Getters and Setters">
        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
        //</editor-fold>
    }

    public static class Google {
        @SerializedName("jwt")
        private String authCode;

        //<editor-fold desc="Getters and Setters">
        public String getAuthCode() {
            return authCode;
        }

        public void setAuthCode(String authCode) {
            this.authCode = authCode;
        }
        //</editor-fold>
    }
}

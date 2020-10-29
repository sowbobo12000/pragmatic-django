package com.fresconews.fresco.framework.network.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Blaze on 8/24/2016.
 */
public class NetworkSocialLinks {

    @SerializedName("twitter")
    private String twitter;

    @SerializedName("facebook")
    private String facebook;

    @SerializedName("google")
    private String google;

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getGoogle() {
        return google;
    }

    public void setGoogle(String google) {
        this.google = google;
    }

}

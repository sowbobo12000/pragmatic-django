package com.fresconews.fresco.framework.persistence.managers;

import android.content.Context;

import com.fresconews.fresco.BuildConfig;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.StringUtils;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Blaze on 9/5/2016.
 */
public class MixpanelManager {

    private static final String TAG = MixpanelManager.class.getSimpleName();
    private static final String OLD_PROJECT_TOKEN = "910fb359b3c1095003a04597e434dd85";
    private static final String DEVELOPMENT_PROJECT_TOKEN = "5201079ebeb0955e2a4f7c547520016d";
    private static final String PRODUCTION_PROJECT_TOKEN = "0e6b9d8ade7a9f56bf6ddae4b0374148";

    private Context mContext;
    private MixpanelAPI mixpanel;

    public MixpanelManager(Context context) {
        mContext = context;
        if (BuildConfig.DEBUG) {
            mixpanel = MixpanelAPI.getInstance(mContext, DEVELOPMENT_PROJECT_TOKEN);
        }
        else {
            mixpanel = MixpanelAPI.getInstance(mContext, PRODUCTION_PROJECT_TOKEN);
        }
        LogUtils.i(TAG, "Created mixpanel instance");
    }

    public void trackUser(String userId) {
        mixpanel.identify(userId);
        mixpanel.getPeople().identify(userId);
        mixpanel.getPeople().set("fresco_id", userId);
        LogUtils.i(TAG, "Track user!!!");

//        JSONObject props = new JSONObject();
//        try {
//            props.put("userId", userId);
//        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
//        mixpanel.getPeople().set(props);
//        mixpanel.getPeople().set("Plan", "Premium");
    }

    public void trackUser(String userId, String username, String fullname, String email) {
        mixpanel.identify(userId);
        mixpanel.getPeople().identify(userId);
//        mixpanel.getPeople().set("fresco_id", userId);

        JSONObject props = new JSONObject();
        try {
            props.put("fresco_id", userId);
            props.put("username", username);
            props.put("fullname", fullname);
            props.put("email", email);

            //Built in mixpanel stuff:
//            "$first_name": "Joe",
//            "$last_name": "Doe",
//            "$created": "2013-04-01T09:02:00",
//            "$email": "joe.doe@example.com"

            props.put("$first_name", fullname);
            props.put("$email", email);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.getPeople().set(props);
        LogUtils.i(TAG, "Track user2!!!");
    }

    //-------------------------------------ONBOARDING------------------------------------------//
    public void beganOnboarding() {
        mixpanel.track("Onboarding");
    }

    public void completedOnboarding() {
        mixpanel.track("Onboarding reads");
    }

    public void exitedOnboarding() {
        mixpanel.track("Onboarding immediate quits");
        mixpanel.flush();
    }

    //-------------------------------------SIGNUP------------------------------------------//
    public void signedUpWithFacebook() {
        //User signs up with Facebook.
        mixpanel.track("Signups with Facebook");
    }

    public void signedUpWithTwitter() {
        //User signs up with Twitter.
        mixpanel.track("Signups with Twitter");

    }

    public void signedUpWithEmail() {
        //User signs up with Email.
        mixpanel.track("Signups with email");
    }

    public void loggedIn(String userId) {
        mixpanel.track("Logins");
    }

    public void signupRadiusSet(double radius) {
        // User sets area in which they will be notified of new assignments.
        JSONObject props = new JSONObject();
        try {
            props.put("radius", radius);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Signup radius changes", props);
    }

    //-------------------------------------CAMERA------------------------------------------//
//    public void openedCamera(){   //merged into "Camera session"
//        mixpanel.track("Camera opens");
//    }

    public void closedCamera(long duration) {
        //The amount of time a user spends in the camera_white_64dp in one session.
        //A session begins when the camera_white_64dp is opened and ends when closed.
        JSONObject props = new JSONObject();
        try {
            props.put("activity_duration", duration);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Camera session", props);
    }

    public void attemptedToUseCameraInPortrait() {
        //User attempts to record video in portrait. The user is wrong.
        mixpanel.track("Portrait video attempts");
    }

    public void videoDuration(long duration) {
        //The length of video that the user recorded.
        // User sets area in which they will be notified of new assignments.
        JSONObject props = new JSONObject();
        try {
            props.put("activity_duration", duration);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Camera sesh video duration", props);
    }

    public void photoCount(int photoCount) {
        //Should be within closed camera_white_64dp
        //The number of photos taken in one session.
        //A session begins when the camera_white_64dp is opened and ends when closed.
        JSONObject props = new JSONObject();
        try {
            props.put("count", photoCount);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Camera sesh photo count", props);
    }

    //-------------------------------------SUBMISSION------------------------------------------//
    public void finisedWritingSubmissionCaption(long duration) {
        //The amount of time it takes for a user to write a caption.
        JSONObject props = new JSONObject();
        try {
            props.put("activity_duration", duration);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Submission time writing caption", props);
    }

    public void submittedContent() {
        //User submits content.
        mixpanel.track("Submissions");
    }

    public void submissionItemsInGallery(int numberOfPhotosAndVideos) {
        //The total number of photos and videos visible in the submission gallery that are able to be submitted.
        JSONObject props = new JSONObject();
        try {
            props.put("count", numberOfPhotosAndVideos);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Submission items in gallery", props);
    }

    public void photosSubmitted(int numberOfPhotosSubmitted) {
        //The number of photos submitted.
        JSONObject props = new JSONObject();
        try {
            props.put("count", numberOfPhotosSubmitted);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Submission photos in gallery", props);

    }

    public void videosSubmitted(int numberOfVideosSubmitted) {
        //The number  of videos submitted.
        JSONObject props = new JSONObject();
        try {
            props.put("count", numberOfVideosSubmitted);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Submission videos in gallery", props);
    }

    public void contentPurchased() {
        //Content purchased by an outlet.
        mixpanel.track("Purchases");
    }

    public void trackUploadDebug(String message, double kbps) {
        JSONObject props = new JSONObject();
        try {
            props.put("debug_message", message);
            props.put("upload_speed_kBps", kbps);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Upload debug", props);
    }

    public void trackUploadDebug(String message) {
        JSONObject props = new JSONObject();
        try {
            props.put("debug_message", message);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Upload debug", props);
    }

    public void trackUploadError(String error) {
        JSONObject props = new JSONObject();
        try {
            props.put("error_message", error);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Upload error", props);
    }

    public void trackUploadError(String error, double kBps) {
        JSONObject props = new JSONObject();
        try {
            props.put("error_message", error);
            props.put("upload_speed_kBps", kBps);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Upload error", props);
    }

    //-------------------------------------HIGHLIGHTS------------------------------------------//
    public void highlightsScreenClosed(long duration, int galleriesScrolledBy) {
        //The amount of time a user spends browsing Highlights in a session.
        //A session begins when opening the Highlights tab and ends when the user navigates to another screen.
        JSONObject props = new JSONObject();
        try {
            props.put("activity_duration", duration);
            props.put("count", galleriesScrolledBy);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Highlights session", props);
    }

    public void galleryOpenedFromHighlights(String galleryId) {
        //User taps “Read More” when browsing the Highlights tab.
        JSONObject props = new JSONObject();
        try {
            props.put("gallery_id", galleryId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Galleries opened from highlights", props);
    }

    public void gallerySharedFromHighlights(String galleryId) {
        //User taps the share icon when browsing the Highlights tab.
        JSONObject props = new JSONObject();
        try {
            props.put("gallery_id", galleryId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Galleries shared from highlights", props);
    }

    public void articleOpened(String articleId) {
        //User taps on Article underneath Gallery
        JSONObject props = new JSONObject();
        try {
            props.put("article_id", articleId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Article opens", props);
    }

    public void articleOpened(String articleId, String articleUrl) {
        //User taps on Article underneath Gallery

    }

    //-------------------------------------GALLERY-----------------------------------------//
    public void galleryOpened(String galleryId, String openedFrom, String userId) {

    }

    public void gallerySession(String galleryId, String authorId, long duration, int percentScrolled, Date highlightedAt, String openedFrom, JSONArray tags) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen
        JSONObject props = new JSONObject();
        try {
            props.put("gallery_id", galleryId);
            props.put("activity_duration", duration);
            props.put("percent_scrolled", percentScrolled);
            props.put("author", authorId);
            props.put("date", highlightedAt);
            props.put("opened_from", openedFrom);
//            props.put("tags", tags); //elmir doesn't want them since phil didn't add them
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Gallery session", props);
    }

    public void galleryLiked(String galleryId, String openedFrom, String userId) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen
        JSONObject props = new JSONObject();
        try {
            props.put("gallery_id", galleryId);
            if (StringUtils.toNullIfEmpty(userId) == null) {
                props.put("user_id", userId);
            }
            props.put("liked_from", openedFrom);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Gallery Liked", props);
    }

    public void galleryDisliked(String galleryId, String openedFrom, String userId) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen
        JSONObject props = new JSONObject();
        try {
            props.put("gallery_id", galleryId);
            if (StringUtils.toNullIfEmpty(userId) == null) {
                props.put("user_id", userId);
            }
            props.put("disliked_from", openedFrom);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Gallery Disliked", props);
    }

    public void galleryShared(String galleryId, String openedFrom, String userId) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen
        JSONObject props = new JSONObject();
        try {
            props.put("gallery_id", galleryId);
            if (StringUtils.toNullIfEmpty(userId) == null) {
                props.put("user_id", userId);
            }
            props.put("shared_from", openedFrom);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Gallery Shared", props);
    }

    public void galleryReposted(String galleryId, String openedFrom, String userId) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen
        JSONObject props = new JSONObject();
        try {
            props.put("gallery_id", galleryId);
            if (StringUtils.toNullIfEmpty(userId) == null) {
                props.put("user_id", userId);
            }
            props.put("reposted_from", openedFrom);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Gallery Reposted", props);
    }

    public void galleryUnreposted(String galleryId, String openedFrom, String userId) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen
        JSONObject props = new JSONObject();
        try {
            props.put("gallery_id", galleryId);
            if (StringUtils.toNullIfEmpty(userId) == null) {
                props.put("user_id", userId);
            }
            props.put("unreposted_from", openedFrom);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Gallery Unreposted", props);
    }

    //-------------------------------------STORIES------------------------------------------//
    public void storiesScreenClosed(long duration, int storiesScrolledBy) {
        // The amount of time a user spends browsing Stories in a session.
        //A session begins when opening the Stories tab and ends when the user navigates to another screen.
        JSONObject props = new JSONObject();
        try {
            props.put("activity_duration", duration);
            props.put("count", storiesScrolledBy);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Stories session", props);
    }

    //    public void storiesScrolledBy(int storiesScrolledBy){ //MERGED WITH ABOVE
//        //The number of stories a user scrolls past in a session.
//        //A session begins when opening the Highlights tab and ends when the user closes the app.
//        JSONObject props = new JSONObject();
//        try {
//            props.put("count", storiesScrolledBy);
//        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
//        mixpanel.track("Stories sesh galleries seen", props);
//    }
    public void galleriesOpenedFromStories(String galleryId) {
        //User taps “Read More” when browsing the Stories tab.
        //User taps the share icon when browsing the Highlights tab.
        JSONObject props = new JSONObject();
        try {
            props.put("gallery_id", galleryId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Galleries opened from stories", props);
    }

    public void gallerySharedFromStories(String galleryId) {
        //User taps the share icon when browsing the Stories tab.
        //User taps the share icon when browsing the Highlights tab.
        JSONObject props = new JSONObject();
        try {
            props.put("gallery_id", galleryId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Galleries shared from stories", props);
    }

    //-------------------------------------PROFILE------------------------------------------//
    public void profileClosed(long duration, int galleriesScrolledBy, String userId) {
        JSONObject props = new JSONObject();
        try {
            props.put("activity_duration", duration);
            props.put("count", galleriesScrolledBy);
            props.put("user_id", userId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Profile session", props);
    }

    //    public void galleriesScrolledByInProfile(int galleriesScrolledBy, String userId){ //MERGERD WITH ABOVE
//        //The number of stories a user scrolls past in a session.
//        //A session begins when opening the Highlights tab and ends when the user closes the app.
//        JSONObject props = new JSONObject();
//        try {
//            props.put("count", galleriesScrolledBy);
//            props.put("user_id", userId);
//        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
//        mixpanel.track("Profile sesh galleries seen", props);
//    }
    public void galleriesOpenedFromProfile(String galleryId, String userId) {
        //User taps “Read More” when browsing the Stories tab.
        JSONObject props = new JSONObject();
        try {
            props.put("gallery_id", galleryId);
            props.put("user_id", userId);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Galleries opened from profile", props);
    }

    public void gallerySharedFromProfile(String galleryId, String userId) {
        //User taps the share icon when browsing the Stories tab.
        JSONObject props = new JSONObject();
        try {
            props.put("gallery_id", galleryId);
            props.put("user_id", userId);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("Galleries shared from profile", props);
    }

    //-----------------------------------PERMISSIONS------------------------------------------//

    public void permissionLocationEnabled() {
        mixpanel.track("Permissions location enables");
    }

    public void permissionLocationDisabled() {
        mixpanel.track("Permissions location disables");
    }

    public void permissionNotificationEnabled() {
        mixpanel.track("Permissions notification enables");

    }

    public void permissionNotificationDisabled() {
        mixpanel.track("Permissions notification disables");

    }

    public void permissionCameraEnabled() {
        mixpanel.track("Permissions camera enables");

    }

    public void permissionCameraDisabled() {
        mixpanel.track("Permissions camera disables");

    }

    public void permissionMicrophoneEnabled() {
        mixpanel.track("Permissions microphone enables");

    }

    public void permissionMicrophoneDisabled() {
        mixpanel.track("Permissions microphone disables");

    }

    public void permissionPhotosEnabled() {
        mixpanel.track("Permissions photos enables");
    }

    public void permissionPhotosDisabled() {
        mixpanel.track("Permissions photos disables");
    }
}

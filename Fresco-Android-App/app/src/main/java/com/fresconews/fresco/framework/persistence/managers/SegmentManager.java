package com.fresconews.fresco.framework.persistence.managers;

import android.content.Context;

import com.fresconews.fresco.BuildConfig;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.StringUtils;
import com.localytics.android.Localytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.android.integrations.flurry.*;
import com.segment.analytics.android.integrations.flurry.FlurryIntegration;
import com.segment.analytics.android.integrations.localytics.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Blaze on 12/12/2016.
 */
public class SegmentManager {

    private static final String TAG = SegmentManager.class.getSimpleName();
    private static final String DEVELOPMENT_WRITE_KEY = "BxBxiC2CafzGS6FKHK5Ipt5ENDSnVogm";
    private static final String PRODUCTION_WRITE_KEY = "4JOKD0syCXoRuzvD4Ipn6x8bZdQW2qrX";
    private static final String LOCALYTICS_APP_KEY_DEV = "ef0ab046c5a033124da57f1-7b9f2f80-c7ca-11e6-7ce2-006c194ea82c";
    private static final String LOCALYTICS_APP_KEY_PROD = "fc08e005a2b04b340020cbe-5e4f7250-c307-11e6-db99-001660e79be1";
    private static final String FLURRY_APP_KEY_DEV = "WVF75WCM8TZV657TPG8F";
    private static final String FLURRY_APP_KEY_PROD = "XWNC883DZCX9KM5VGX3D";

    private Context context;
    private Analytics analytics;

    public SegmentManager(Context context) {
        this.context = context;

        if (BuildConfig.DEBUG) {
            // Create an analytics client with the given context and Segment write key.
            analytics = new Analytics.Builder(context, DEVELOPMENT_WRITE_KEY)
                    .trackApplicationLifecycleEvents() // Enable this to record certain application events automatically!
                    .use(LocalyticsIntegration.FACTORY)
                    .use(FlurryIntegration.FACTORY)
                    .build();
        }
        else {
            // Create an analytics client with the given context and Segment write key.
            analytics = new Analytics.Builder(context, PRODUCTION_WRITE_KEY)
                    .trackApplicationLifecycleEvents() // Enable this to record certain application events automatically!
                    .use(LocalyticsIntegration.FACTORY)
                    .use(FlurryIntegration.FACTORY)
                    .build();
        }

        // Set the initialized instance as a globally accessible instance.
        Analytics.setSingletonInstance(analytics); // Must be called before any calls to Analytics.with(context)

        ValueMap valueMap = new ValueMap();
        if (BuildConfig.DEBUG) {
            valueMap.put("appKey", LOCALYTICS_APP_KEY_DEV);
            valueMap.put("apiKey", FLURRY_APP_KEY_DEV);
        }
        else {
            valueMap.put("appKey", LOCALYTICS_APP_KEY_PROD);
            valueMap.put("apiKey", FLURRY_APP_KEY_PROD);
        }

        LocalyticsIntegration.FACTORY.create(valueMap, analytics);
        FlurryIntegration.FACTORY.create(valueMap, analytics);

        // Now Analytics.with will return the custom instance
        Analytics.with(context).track("App Launched");

        // Now Analytics.with will return the custom instance
        LogUtils.i(TAG, "Created segment instance");
    }

    public void trackUser(String userId) {
        if (StringUtils.toNullIfEmpty(userId) != null) {
            Analytics.with(context).identify(userId);
        }
    }

    public void trackUser(String userId, String username, String fullname, String email) {
        // Initially when you only know the user's name
        Analytics.with(context).identify(userId, new Traits()
                        .putName(fullname)
                        .putEmail(email)
                        .putUsername(username),
                null);
    }

    //-------------------------------------SCREEN TRACKING------------------------------------------//
    public void trackScreen(String screenName, String feedType) {
        if (screenName.equals("Home")) {
            Analytics.with(context).screen("Home", "Home", new Properties().putValue("feed_type", feedType));
        }
        else {
            Analytics.with(context).screen(null, screenName);
        }
    }

    public void screenDebug(String screenName, String debugMessage, String galleryId, String commentId) {
        Analytics.with(context).track("Screen debug", new Properties()
                .putValue("screen_name", screenName)
                .putValue("debug_message", debugMessage)
                .putValue("comment_id", commentId)
                .putValue("gallery_id", galleryId));
    }

    //-------------------------------------ASSIGNMENT TRACKING------------------------------------------//
    public void assignmentAccepted(String assignmentId, double distanceAway) {
        Analytics.with(context).track("Assignment accepted", new Properties()
                .putValue("assignment_id", assignmentId)
                .putValue("distance_away", distanceAway));
    }

    public void assignmentUnaccepted(String assignmentId, double distanceAway) {
        Analytics.with(context).track("Assignment unaccepted", new Properties()
                .putValue("assignment_id", assignmentId)
                .putValue("distance_away", distanceAway));
    }

    public void assignmentClicked(String assignmentId, double distanceAway) {
        Analytics.with(context).track("Assignment clicked", new Properties()
                .putValue("assignment_id", assignmentId)
                .putValue("distance_away", distanceAway));
    }

    public void assignmentDismissed(String assignmentId, double distanceAway) {
        Analytics.with(context).track("Assignment dismissed", new Properties()
                .putValue("assignment_id", assignmentId)
                .putValue("distance_away", distanceAway));
    }

    //-------------------------------------NOTIFICATIONS------------------------------------------//
    public void notificationReceived(String pushKey, String objectId, String objectType) {
        Analytics.with(context).track("Notification received", new Properties()
                .putValue("push_key", pushKey)
                .putValue("object_id", objectId)
                .putValue("object", objectType));
    }

    public void notificationOpened(String pushKey, String objectId, String objectType) {
        Analytics.with(context).track("Notification opened", new Properties()
                .putValue("push_key", pushKey)
                .putValue("object_id", objectId)
                .putValue("object", objectType));
    }

    //-------------------------------------ONBOARDING------------------------------------------//
    public void beganOnboarding() {
        Analytics.with(context).track("Onboarding");
    }

    public void completedOnboarding() {
        Analytics.with(context).track("Onboarding reads");
    }

    public void exitedOnboarding() {
        Analytics.with(context).track("Onboarding immediate quits");
    }

    //-------------------------------------SIGNUP------------------------------------------//
    public void signedUp(String socialLinks) {
        if (socialLinks != null) {
            Analytics.with(context).track("Signup",
                    new Properties().putValue("social_links", socialLinks));
        }
        else {
            Analytics.with(context).track("Signup",
                    new Properties().putValue("social_links", "none"));
        }

    }

    public void loggedIn(String userId, String platform) {
        if (platform == null) {
            Analytics.with(context).track("Login",
                    new Properties().putValue("platform", "email"));
        }
        else {
            Analytics.with(context).track("Login",
                    new Properties().putValue("platform", platform));
        }

    }

    public void signupRadiusSet(double radius) {
        // User sets area in which they will be notified of new assignments.

        Analytics.with(context).track("Signup radius changes",
                new Properties().putValue("radius", radius));

    }

    //-------------------------------------CAMERA------------------------------------------//
//    public void openedCamera(){   //merged into "Camera session"
//        mixpanel.track("Camera opens");
//    }

    public void closedCamera(long duration) {
        //The amount of time a user spends in the camera_white_64dp in one session.
        //A session begins when the camera_white_64dp is opened and ends when closed.
        Analytics.with(context).track("Camera session",
                new Properties().putValue("activity_duration", duration));

    }

    public void attemptedToUseCameraInPortrait() {
        //User attempts to record video in portrait. The user is wrong.
        Analytics.with(context).track("Portrait video attempts");

    }

    public void videoDuration(long duration) {
        //The length of video that the user recorded.
        // User sets area in which they will be notified of new assignments.

        Analytics.with(context).track("Camera session video duration",
                new Properties().putValue("activity_duration", duration));

    }

    public void photoCount(int photoCount) {
        //Should be within closed camera_white_64dp
        //The number of photos taken in one session.
        //A session begins when the camera_white_64dp is opened and ends when closed.

        Analytics.with(context).track("Camera session photo count",
                new Properties().putValue("count", photoCount));

    }

    //-------------------------------------SUBMISSION------------------------------------------//
    public void finisedWritingSubmissionCaption(long duration) {
        //The amount of time it takes for a user to write a caption.
        Analytics.with(context).track("Submission time writing caption",
                new Properties().putValue("activity_duration", duration));
    }

    public void submittedContent(int photos, int videos, String assignmentId) {
        //User submits content.
        Analytics.with(context).track("Submissions", new Properties()
                .putValue("photos_submitted ", photos)
                .putValue("videos_submitted ", videos)
                .putValue("assignment_id", assignmentId));
    }

    public void submissionItemsInGallery(int numberOfPhotosAndVideos) {
        //The total number of photos and videos visible in the submission gallery that are able to be submitted.
        Analytics.with(context).track("Submission items in gallery",
                new Properties().putValue("count", numberOfPhotosAndVideos));
    }

    public void photosSubmitted(int numberOfPhotosSubmitted) {
        //The number of photos submitted.
        Analytics.with(context).track("Submission photos in gallery",
                new Properties().putValue("count", numberOfPhotosSubmitted));

    }

    public void videosSubmitted(int numberOfVideosSubmitted) {
        //The number  of videos submitted.
        Analytics.with(context).track("Submission videos in gallery",
                new Properties().putValue("count", numberOfVideosSubmitted));

    }

    public void contentPurchased() {
        //Content purchased by an outlet.
        Analytics.with(context).track("Purchases");
    }

    public void trackUploadDebug(String message, double kBps) {
        Analytics.with(context).track("Upload debug",
                new Properties().putValue("upload_speed_kBps", kBps)
                                .putValue("debug_message", message));
    }

    public void trackUploadDebug(String message) {
        Analytics.with(context).track("Upload debug",
                new Properties().putValue("debug_message", message));

    }

    public void trackTranscodeDebug(double fileKbps, double targetKbps) {
        Analytics.with(context).track("Transcode debug",
                new Properties().putValue("file_kbps", fileKbps)
                                .putValue("target_kbps", targetKbps));
    }

    public void trackTranscodeError(int videosCount, long totalBytes, int totalVideoDuration, int totalTranscodingDuration) {
        if (totalTranscodingDuration != 0) {
            Analytics.with(context).track("Transcode error",
                    new Properties().putValue("videos_count", videosCount)
                                    .putValue("total_bytes", totalBytes)
                                    .putValue("total_duration", totalVideoDuration)
                                    .putValue("transcoding_duration", totalTranscodingDuration)
                                    .putValue("transcoding_kBps", totalBytes / totalTranscodingDuration));
        }
        else {
            Analytics.with(context).track("Transcode error",
                    new Properties().putValue("videos_count", videosCount)
                                    .putValue("total_bytes", totalBytes)
                                    .putValue("total_duration", totalVideoDuration)
                                    .putValue("transcoding_duration", totalTranscodingDuration));
        }
    }

    public void trackUploadError(String error) {
        Analytics.with(context).track("Upload error", new Properties().putValue("error_message", error));

    }

    public void trackUploadError(String error, double kBps) {
        Analytics.with(context).track("Upload error", new Properties()
                .putValue("error_message", error)
                .putValue("upload_speed_kBps", kBps));
    }

    public void trackGeocoding(String error) {
        Analytics.with(context).track("Geocoding error", new Properties().putValue("error_message", error));

    }

    //-------------------------------------HIGHLIGHTS------------------------------------------//
    public void highlightsScreenClosed(long duration, int galleriesScrolledBy) {
        //The amount of time a user spends browsing Highlights in a session.
        //A session begins when opening the Highlights tab and ends when the user navigates to another screen.
        Analytics.with(context).track("Highlights session", new Properties()
                .putValue("activity_duration", duration)
                .putValue("galleries_scrolled_past", galleriesScrolledBy));

    }

    public void galleryOpenedFromHighlights(String galleryId) {
        //User taps “Read More” when browsing the Highlights tab.
        Analytics.with(context).track("Galleries opened from highlights", new Properties()
                .putValue("gallery_id", galleryId));

    }

    public void gallerySharedFromHighlights(String galleryId) {
        //User taps the share icon when browsing the Highlights tab.
        Analytics.with(context).track("Galleries shared from highlights", new Properties().putValue("gallery_id", galleryId));

    }

    public void articleOpened(String articleId) {
        //User taps on Article underneath Gallery

        Analytics.with(context).track("Article opens", new Properties().putValue("article_id", articleId));

    }

    public void articleOpened(String articleId, String articleUrl) {
        //User taps on Article underneath Gallery

        Analytics.with(context).track("Article opens", new Properties()
                .putValue("article_id", articleId)
                .putValue("article_url", articleUrl));

    }

    //-------------------------------------GALLERY-----------------------------------------//
    public void galleryOpened(String galleryId, String openedFrom, String userId) {
        if (userId != null) {
            Analytics.with(context).track("Gallery opened", new Properties()
                    .putValue("gallery_id", galleryId)
                    .putValue("user_id", userId)
                    .putValue("opened_from", openedFrom));
        }
        else {
            Analytics.with(context).track("Gallery opened", new Properties()
                    .putValue("gallery_id", galleryId)
                    .putValue("opened_from", openedFrom));
        }
    }

    public void gallerySession(String galleryId, String authorId, long duration, int percentScrolled, Date highlightedAt, String openedFrom, JSONArray tags) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen

        Analytics.with(context).track("Gallery session", new Properties()
                .putValue("gallery_id", galleryId)
                .putValue("activity_duration", duration)
                .putValue("scrolled_percent", percentScrolled)
                .putValue("author", authorId)
                .putValue("date", highlightedAt)
                .putValue("opened_from", openedFrom));

    }

    public void galleryLiked(String galleryId, String openedFrom, String userId) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen

        if (userId != null) {
            Analytics.with(context).track("Gallery liked", new Properties()
                    .putValue("gallery_id", galleryId)
                    .putValue("user_id", userId)
                    .putValue("liked_from", openedFrom));
        }
        else {
            Analytics.with(context).track("Gallery liked", new Properties()
                    .putValue("gallery_id", galleryId)
                    .putValue("liked_from", openedFrom));
        }
    }

    public void galleryDisliked(String galleryId, String openedFrom, String userId) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen

        if (userId != null) {
            Analytics.with(context).track("Gallery unliked", new Properties()
                    .putValue("gallery_id", galleryId)
                    .putValue("user_id", userId)
                    .putValue("disliked_from", openedFrom));
        }
        else {
            Analytics.with(context).track("Gallery unliked", new Properties()
                    .putValue("gallery_id", galleryId)
                    .putValue("disliked_from", openedFrom));
        }

    }

    public void galleryShared(String galleryId, String openedFrom, String userId) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen

        if (userId != null) {
            Analytics.with(context).track("Gallery shared", new Properties()
                    .putValue("gallery_id", galleryId)
                    .putValue("user_id", userId)
                    .putValue("shared_from", openedFrom));
        }
        else {
            Analytics.with(context).track("Gallery shared", new Properties()
                    .putValue("gallery_id", galleryId)
                    .putValue("shared_from", openedFrom));
        }
    }

    public void galleryReposted(String galleryId, String openedFrom, String userId) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen

        if (userId != null) {
            Analytics.with(context).track("Gallery reposted", new Properties()
                    .putValue("gallery_id", galleryId)
                    .putValue("user_id", userId)
                    .putValue("reposted_from", openedFrom));
        }
        else {
            Analytics.with(context).track("Gallery reposted", new Properties()
                    .putValue("gallery_id", galleryId)
                    .putValue("reposted_from", openedFrom));
        }

    }

    public void galleryUnreposted(String galleryId, String openedFrom, String userId) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen
        LogUtils.i(TAG, "track unreposted");
        if (userId != null) {
            Analytics.with(context).track("Gallery unreposted", new Properties()
                    .putValue("gallery_id", galleryId)
                    .putValue("user_id", userId)
                    .putValue("unreposted_from", openedFrom));
        }
        else {
            Analytics.with(context).track("Gallery unreposted", new Properties()
                    .putValue("gallery_id", galleryId)
                    .putValue("unreposted_from", openedFrom));
        }

    }

    //-------------------------------------POST SESSION------------------------------------------//
    public void postSession(String postId, String postIdSwipedFrom, String galleryId, boolean inList, long duration,
                            boolean isVideo, int videoDuration, boolean postMuted) {
        //Will check and update a post session based off of the current postId and galleryId in the manager.
        if (postIdSwipedFrom != null) {
            if (isVideo) {
                Analytics.with(context).track("Post session", new Properties()
                        .putValue("post_id", postId)
                        .putValue("post_id_swiped_from", postIdSwipedFrom)
                        .putValue("gallery_id", galleryId)
                        .putValue("in_list", inList)
                        .putValue("duration", duration)
                        .putValue("video", isVideo)
                        .putValue("video_duration", videoDuration)
                        .putValue("video_unmuted", !postMuted)
                        .putValue("in_list", inList));
            }
            else {
                Analytics.with(context).track("Post session", new Properties()
                        .putValue("post_id", postId)
                        .putValue("post_id_swiped_from", postIdSwipedFrom)
                        .putValue("gallery_id", galleryId)
                        .putValue("video", isVideo)
                        .putValue("duration", duration)
                        .putValue("in_list", inList));
            }
        }
        else {
            if (isVideo) {
                Analytics.with(context).track("Post session", new Properties()
                        .putValue("post_id", postId)
                        .putValue("gallery_id", galleryId)
                        .putValue("in_list", inList)
                        .putValue("duration", duration)
                        .putValue("video", isVideo)
                        .putValue("video_duration", videoDuration)
                        .putValue("video_unmuted", !postMuted)
                        .putValue("in_list", inList));
            }
            else {
                Analytics.with(context).track("Post session", new Properties()
                        .putValue("post_id", postId)
                        .putValue("gallery_id", galleryId)
                        .putValue("video", isVideo)
                        .putValue("duration", duration)
                        .putValue("in_list", inList));
            }
        }

    }

    public void mutePost(String postId) {

    }

    //-------------------------------------STORIES------------------------------------------//
    public void storiesScreenClosed(long duration, int storiesScrolledBy) {
        // The amount of time a user spends browsing Stories in a session.
        //A session begins when opening the Stories tab and ends when the user navigates to another screen.

        Analytics.with(context).track("Stories session", new Properties()
                .putValue("activity_duration", duration)
                .putValue("stories_scrolled_past", storiesScrolledBy));
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

        Analytics.with(context).track("Galleries opened from stories", new Properties()
                .putValue("gallery_id", galleryId));

    }

    public void gallerySharedFromStories(String galleryId) {
        //User taps the share icon when browsing the Stories tab.
        //User taps the share icon when browsing the Highlights tab.

        Analytics.with(context).track("Galleries shared from stories", new Properties()
                .putValue("gallery_id", galleryId));

    }

    //-------------------------------------PROFILE------------------------------------------//
    public void profileClosed(long duration, int galleriesScrolledBy, String userId) {

        Analytics.with(context).track("Profile session", new Properties()
                .putValue("activity_duration", duration)
                .putValue("galleries_scrolled_past", galleriesScrolledBy)
                .putValue("user_id", userId));

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

        Analytics.with(context).track("Galleries opened from profile", new Properties()
                .putValue("gallery_id", galleryId)
                .putValue("user_id", userId));

    }

    public void gallerySharedFromProfile(String galleryId, String userId) {
        //User taps the share icon when browsing the Stories tab.

        Analytics.with(context).track("Galleries shared from profile", new Properties()
                .putValue("gallery_id", galleryId)
                .putValue("user_id", userId));

    }

    //-----------------------------------PERMISSIONS------------------------------------------//

    public void permissionLocationEnabled() {
        Analytics.with(context).track("Permissions location enables");

    }

    public void permissionLocationDisabled() {
        Analytics.with(context).track("Permissions location disables");

    }

    public void permissionNotificationEnabled() {
        Analytics.with(context).track("Permissions notification enables");

    }

    public void permissionNotificationDisabled() {
        Analytics.with(context).track("Permissions notification disables");

    }

    public void permissionCameraEnabled() {
        Analytics.with(context).track("Permissions camera enables");

    }

    public void permissionCameraDisabled() {
        Analytics.with(context).track("Permissions camera disables");

    }

    public void permissionMicrophoneEnabled() {
        Analytics.with(context).track("Permissions microphone enables");

    }

    public void permissionMicrophoneDisabled() {
        Analytics.with(context).track("Permissions microphone disables");

    }

    public void permissionPhotosEnabled() {
        Analytics.with(context).track("Permissions photos enables");

    }

    public void permissionPhotosDisabled() {
        Analytics.with(context).track("Permissions photos disables");

    }
}

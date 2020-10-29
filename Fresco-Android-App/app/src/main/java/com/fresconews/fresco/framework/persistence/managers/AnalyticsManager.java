package com.fresconews.fresco.framework.persistence.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.crashlytics.android.answers.SignUpEvent;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.framework.network.responses.NetworkUser;
import com.fresconews.fresco.framework.persistence.models.Post;
import com.fresconews.fresco.framework.persistence.models.Post_Table;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.crashlytics.android.answers.Answers;
import com.fresconews.fresco.v2.utils.StringUtils;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.json.JSONArray;

import java.io.File;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by Blaze on 9/5/2016.
 */
public class AnalyticsManager {

    private static final String TAG = AnalyticsManager.class.getSimpleName();

    private Context context;
    private SegmentManager segmentManager;
    private UserManager userManager;

    private long cameraDuration;
    private long captionWritingDuration;
    private long highlightsScreenSessionDuration;
    private long galleryDetailSessionDuration;
    private long storiesScreenSessionDuration;
    private long profileScreenSessionDuration;
    private long postSessionDuration;
    private boolean trackingUser = false;
    private Boolean finishedOnBoarding = false;
    private Boolean beganOnBoarding = false;
    private boolean signedUpWithFacebook = false;
    private boolean signedUpWithTwitter = false;
    private boolean signedUpWithGoogle = false;
    private boolean browsingHighlights = false;
    private boolean browsingStories = false;
    private boolean browsingProfile = false;
    private boolean inList;
    private boolean postMuted = true;
    private int totalGalleriesScrolledBy = 0;
    private int totalStoriesScrolledBy = 0;
    private String openedFrom = "";
    private String postId = "";
    private String previousPostId = "";
    private String galleryId = "";
    private String selectedAssignment = "";
    private double selectedAssignmentDistance = 0;

    private static final String ANALYTICS_PERMISSIONS = "FRESCO_PERMISSIONS";
    private static final String LOCATION_PERMISSION = "android.permission.ACCESS_FINE_LOCATION";
    private static final String NOTIFICATION_PERMISSION = "com.fresconews.fresco.permission.C2D_MESSAGE";
    private static final String CAMERA_PERMISSION = "android.permission.CAMERA";
    private static final String MIC_PERMISSION = "android.permission.RECORD_AUDIO";
    private static final String PHOTO_PERMISSION = "android.permission.READ_EXTERNAL_STORAGE";

    public AnalyticsManager(Context context, UserManager userManager) {
        this.context = context;
        this.userManager = userManager;
        segmentManager = new SegmentManager(this.context);
    }

    public void trackUser(String userId) {
        if (userId == null) {
            return;
        }
        trackingUser = true;

        //Add in email and name to tracking
        NetworkUser me = userManager.getNetworkUserMe();
        if (me != null && me.getFullName() != null && me.getEmail() != null && me.getUsername() != null) {
            segmentManager.trackUser(userId, me.getUsername(), me.getFullName(), me.getEmail());
        }
        else {
            segmentManager.trackUser(userId);
        }

    }

    public boolean isTrackingUser() {
        return trackingUser;
    }

    //-------------------------------------SCREEN TRACKING------------------------------------------//
    public void trackScreen(String screenName, String feedType) {
        // no category, name "Photo Feed" and a property "Feed Length"
        segmentManager.trackScreen(screenName, feedType);
    }

    public void trackScreen(String screenName) {
        // no category, name "Photo Feed" and a property "Feed Length"
        segmentManager.trackScreen(screenName, null);
    }

    public void screenDebug(String screenName, String debugMessage, String galleryId, String commentId) {
        segmentManager.screenDebug(screenName, debugMessage, galleryId, commentId);
    }

    //-------------------------------------ASSIGNMENT TRACKING------------------------------------------//
    public void assignmentAccepted(String assignmentId, double distanceAway) {
        if (StringUtils.toNullIfEmpty(assignmentId) == null) {
            return;
        }
        segmentManager.assignmentAccepted(assignmentId, distanceAway);
    }

    public void assignmentUnaccepted(String assignmentId, double distanceAway) {
        if (StringUtils.toNullIfEmpty(assignmentId) == null) {
            return;
        }
        segmentManager.assignmentUnaccepted(assignmentId, distanceAway);
    }

    public void assignmentClicked(String assignmentId, double distanceAway) {
        if (StringUtils.toNullIfEmpty(assignmentId) == null || assignmentId.equals(selectedAssignment)) {
            LogUtils.i(TAG, "set AB clicked but null");
            return;
        }
        if (!assignmentId.equals(selectedAssignment)) {
            LogUtils.i(TAG, "set AB clicked but dismissing first - " + selectedAssignment);
            assignmentDismissed(selectedAssignment, selectedAssignmentDistance);
        }
        LogUtils.i(TAG, "set AB clicked - " + assignmentId);
        this.selectedAssignment = assignmentId;
        this.selectedAssignmentDistance = distanceAway;
        segmentManager.assignmentClicked(assignmentId, distanceAway);
    }

    public void assignmentDismissed(String assignmentId, double distanceAway) {
        if (StringUtils.toNullIfEmpty(assignmentId) == null) {
            LogUtils.i(TAG, "set AC dismissed but null");
            return;
        }
        if (!assignmentId.equals(selectedAssignment)) {
            LogUtils.i(TAG, "set AC dismissed but not equal - " + assignmentId);
            assignmentDismissed(selectedAssignment, selectedAssignmentDistance);
            assignmentClicked(assignmentId, distanceAway);
        }
        LogUtils.i(TAG, "set AC dismissing - " + assignmentId);

        segmentManager.assignmentDismissed(assignmentId, distanceAway);
        selectedAssignment = "";
    }

    //-------------------------------------NOTIFICATIONS------------------------------------------//
    public void notificationReceived(String pushKey, String objectId, String objectType) {
        segmentManager.notificationReceived(pushKey, objectId, objectType);
    }

    public void notificationOpened(String pushKey, String objectId, String objectType) {
        LogUtils.i(TAG, "notification opened");
        segmentManager.notificationOpened(pushKey, objectId, objectType);
    }

    //-------------------------------------ONBOARDING------------------------------------------//
    public void beganOnboarding() {
        //User begins onboard (first screen in the app, where they see instructions)
        segmentManager.beganOnboarding();
        beganOnBoarding = true;
    }

    public void completedOnboarding() {
        //User completes onboard (user enters the normal app / home screen)
        segmentManager.completedOnboarding();
        finishedOnBoarding = true;
    }

    public void exitedOnboarding() {
        //User exits app before completing onboard (user decided they didn’t want to try the app)
        if (beganOnBoarding && !finishedOnBoarding) {
            segmentManager.exitedOnboarding();
            beganOnBoarding = false;
        }
    }

    //-------------------------------------SIGNUP------------------------------------------//
    public void signingUpWithFacebook() {
        //User signs up with Facebook.
        signedUpWithFacebook = true;
    }

    public void signingUpWithTwitter() {
        //User signs up with Twitter.
        signedUpWithTwitter = true;
    }

    public void signingUpWithGoogle() {
        //User signs up with Twitter.
        signedUpWithGoogle = true;
    }

    public void signedUpWithEmail() {
        //User signs up with Email.
        if (signedUpWithGoogle && signedUpWithFacebook && signedUpWithTwitter) {
            segmentManager.signedUp("twitter, facebook, google");
            Answers.getInstance().logSignUp(new SignUpEvent()
                    .putMethod("Email")
                    .putSuccess(true)
                    .putCustomAttribute("twitter", 1)
                    .putCustomAttribute("facebook", 1)
                    .putCustomAttribute("google", 1));
        }
        else if (signedUpWithFacebook && signedUpWithTwitter) {
            segmentManager.signedUp("twitter, facebook");
            Answers.getInstance().logSignUp(new SignUpEvent()
                    .putMethod("Email")
                    .putSuccess(true)
                    .putCustomAttribute("twitter", 1)
                    .putCustomAttribute("facebook", 1)
                    .putCustomAttribute("google", 0));
        }
        else if (signedUpWithGoogle && signedUpWithTwitter) {
            segmentManager.signedUp("twitter, google");
            Answers.getInstance().logSignUp(new SignUpEvent()
                    .putMethod("Email")
                    .putSuccess(true)
                    .putCustomAttribute("twitter", 1)
                    .putCustomAttribute("facebook", 0)
                    .putCustomAttribute("google", 1));
        }
        else if (signedUpWithFacebook && signedUpWithGoogle) {
            segmentManager.signedUp("facebook, google");
            Answers.getInstance().logSignUp(new SignUpEvent()
                    .putMethod("Email")
                    .putSuccess(true)
                    .putCustomAttribute("twitter", 0)
                    .putCustomAttribute("facebook", 1)
                    .putCustomAttribute("google", 1));
        }
        else if (signedUpWithTwitter) {
            segmentManager.signedUp("twitter");
            Answers.getInstance().logSignUp(new SignUpEvent()
                    .putMethod("Email")
                    .putSuccess(true)
                    .putCustomAttribute("twitter", 1)
                    .putCustomAttribute("facebook", 0)
                    .putCustomAttribute("google", 0));
        }
        else if (signedUpWithFacebook) {
            segmentManager.signedUp("facebook");
            Answers.getInstance().logSignUp(new SignUpEvent()
                    .putMethod("Email")
                    .putSuccess(true)
                    .putCustomAttribute("twitter", 0)
                    .putCustomAttribute("facebook", 1)
                    .putCustomAttribute("google", 0));
        }
        else if (signedUpWithGoogle) {
            segmentManager.signedUp("google");
            Answers.getInstance().logSignUp(new SignUpEvent()
                    .putMethod("Email")
                    .putSuccess(true)
                    .putCustomAttribute("twitter", 0)
                    .putCustomAttribute("facebook", 0)
                    .putCustomAttribute("google", 1));
        }
        else {
            segmentManager.signedUp(null);
            Answers.getInstance().logSignUp(new SignUpEvent()
                    .putMethod("Email")
                    .putSuccess(true)
                    .putCustomAttribute("twitter", 0)
                    .putCustomAttribute("facebook", 0)
                    .putCustomAttribute("google", 0));
        }
    }

    public void loggedIn(String userId, String platform) {
        trackUser(userId);
        segmentManager.loggedIn(userId, platform);
    }

    public void signupRadiusSet(double radius) {
        // User sets area in which they will be notified of new assignments.
        segmentManager.signupRadiusSet(radius);
    }

    //-------------------------------------CAMERA------------------------------------------//
    public void openedCamera() {
        cameraDuration = System.currentTimeMillis();
    }

    public void closedCamera() {
        //The amount of time a user spends in the camera_white_64dp in one session.
        //A session begins when the camera_white_64dp is opened and ends when closed.
        long tmpCameraDuration = (System.currentTimeMillis() - cameraDuration) / 1000;
        segmentManager.closedCamera(tmpCameraDuration);
    }

    public void attemptedToUseCameraInPortrait() {
        //User attempts to record video in portrait. The user is wrong.
        segmentManager.attemptedToUseCameraInPortrait();
    }

    public void videoDuration(long duration) {
        //The length of video that the user recorded.
        segmentManager.videoDuration(duration);
    }

    public void photoCount(int photoCount) {
        //Should be within closed camera_white_64dp
        //The number of photos taken in one session.
        //A session begins when the camera_white_64dp is opened and ends when closed.
        segmentManager.photoCount(photoCount);
    }

    //-------------------------------------SUBMISSION------------------------------------------//
    public void startedWritingSubmissionCaption() {
        //The amount of time it takes for a user to write a caption.
        captionWritingDuration = System.currentTimeMillis();
    }

    public void finisedWritingSubmissionCaption() {
        //The amount of time it takes for a user to write a caption.
        captionWritingDuration = (System.currentTimeMillis() - captionWritingDuration) / 1000;
        segmentManager.finisedWritingSubmissionCaption(captionWritingDuration);
    }

    public void submittedContent(int photos, int videos, String assignment) {
        //User submits content.
        segmentManager.submittedContent(photos, videos, assignment);
    }

    public void submissionItemsInGallery(int numberOfPhotosAndVideos) {
        //The total number of photos and videos visible in the submission gallery that are able to be submitted.
        segmentManager.submissionItemsInGallery(numberOfPhotosAndVideos);
    }

    public void photosSubmitted(int numberOfPhotosSubmitted) {
        //The number of photos submitted.
        segmentManager.photosSubmitted(numberOfPhotosSubmitted);
    }

    public void videosSubmitted(int numberOfVideosSubmitted) {
        //The number  of videos submitted.
        segmentManager.videosSubmitted(numberOfVideosSubmitted);
    }

    public void contentPurchased() {
        //Content purchased by an outlet. //todo
    }

    public void trackUploadDebug(String error, double kBps) {
        segmentManager.trackUploadDebug(error, kBps);
    }

    public void trackUploadDebug(String error) {
        segmentManager.trackUploadDebug(error);
    }

    public void trackTranscodeDebug(int fileKbps, int targetKbps) {
        segmentManager.trackTranscodeDebug(fileKbps, targetKbps);
    }

    public void trackTranscodeError(Map<FrescoUploadService2.TranscodeMedia, Double> progressMap, int totalUploadDuration) {
        long totalBytes = 0;
        int videosCount = 0;
        int totalVideoDuration = 0;

        for (Map.Entry<FrescoUploadService2.TranscodeMedia, Double> entry : progressMap.entrySet()) {
            FrescoUploadService2.TranscodeMedia transcodeMedia = entry.getKey();
            totalVideoDuration += transcodeMedia.duration;
            File f = new File(transcodeMedia.uri.getPath());
            long size = f.length();
            totalBytes += size;
            videosCount++;
        }

        segmentManager.trackTranscodeError(videosCount, totalBytes, totalVideoDuration, totalUploadDuration);
    }

    public void trackUploadError(String error, double kBps) {
        segmentManager.trackUploadError(error, kBps);
    }

    public void trackUploadError(String error) {
        segmentManager.trackUploadError(error);
    }

    public void trackGeocodingError(String error) {
        segmentManager.trackGeocoding(error);
    }

    //-------------------------------------HIGHLIGHTS------------------------------------------//
    public void highlightsScreenOpened() {
        //The amount of time a user spends browsing HighlightsHin a session.
        //A session begins when opening the Highlights tab and ends when the user navigates to another screen.
        highlightsScreenSessionDuration = System.currentTimeMillis();
        browsingHighlights = true;
    }

    public void highlightsScreenClosed() {
        //The amount of time a user spends browsing Highlights in a session.
        //A session begins when opening the Highlights tab and ends when the user navigates to another screen.
        if (browsingHighlights) {
            browsingHighlights = false;
            highlightsScreenSessionDuration = (System.currentTimeMillis() - highlightsScreenSessionDuration) / 1000L;
            segmentManager.highlightsScreenClosed(highlightsScreenSessionDuration, totalGalleriesScrolledBy);
            totalGalleriesScrolledBy = 0;
        }
    }

    public void galleriesScrolledByInHighlights(int galleriesScrolledBy) {
        //The number of stories a user scrolls past in a session.
        //A session begins when opening the Highlights tab and ends when the user closes the app.
        this.totalGalleriesScrolledBy += galleriesScrolledBy; //This is sent to mix panel when you leave highlights
    }

    public void galleryOpenedFromHighlights(String galleryId) {
        //User taps “Read More” when browsing the Highlights tab.
        openedFrom = "highlights";
        segmentManager.galleryOpenedFromHighlights(galleryId);
    }

    public void galleryOpened() {
        //The amount of time a user spends browsing HighlightsHin a session.
        //A session begins when opening the Highlights tab and ends when the user navigates to another screen.
        galleryDetailSessionDuration = System.currentTimeMillis();
        browsingHighlights = true;
    }

    public void galleryClosed(String galleryId, String author, int percentScrolled, Date highlightedAt, JSONArray tags) {
        galleryDetailSessionDuration = (System.currentTimeMillis() - galleryDetailSessionDuration) / 1000L;
        segmentManager.gallerySession(galleryId, author, galleryDetailSessionDuration, percentScrolled, highlightedAt, openedFrom, tags);
        openedFrom = "";
    }

    public void gallerySharedFromHighlights(String galleryId) {
        //User taps the share icon when browsing the Highlights tab.
        segmentManager.gallerySharedFromHighlights(galleryId);
    }

    //-------------------------------------GALLERY-----------------------------------------//
    public void galleryOpened(String galleryId, String openedFrom, String userId) {
        this.openedFrom = openedFrom;
        segmentManager.galleryOpened(galleryId, openedFrom, userId);
    }

    public void galleryLiked(String galleryId, String openedFrom, String userId) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen
        segmentManager.galleryLiked(galleryId, openedFrom, userId);
    }

    public void galleryDisliked(String galleryId, String openedFrom, String userId) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen
        segmentManager.galleryDisliked(galleryId, openedFrom, userId);
    }

    public void galleryShared(String galleryId, String openedFrom, String userId) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen
        segmentManager.galleryShared(galleryId, openedFrom, userId);
    }

    public void galleryReposted(String galleryId, String openedFrom, String userId) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen
        segmentManager.galleryReposted(galleryId, openedFrom, userId);
    }

    public void galleryUnreposted(String galleryId, String openedFrom, String userId) {
        //A session begins when a user goes into a gallery detail view and ends when the user leaves the screen
        segmentManager.galleryUnreposted(galleryId, openedFrom, userId);
    }

    //-------------------------------------POST SESSION------------------------------------------//
    public void postSession(String postId, String galleryId, boolean inList) {
        //Will check and update a post session based off of the current postId and galleryId in the manager.
        if (StringUtils.toNullIfEmpty(postId) == null) {
            return;
        }

        //Get the post
        Post post = SQLite.select()
                          .from(Post.class)
                          .where(Post_Table.id.eq(postId))
                          .querySingle();
        if (post == null) {
            return;
        }
        long postSessionDurationDifference = (System.currentTimeMillis() - postSessionDuration) / 1000L;
        if (postSessionDuration == 0) { //just started tracking a new post
            postSessionDuration = System.currentTimeMillis();
            return;
        }

        if (galleryId == null) {
            galleryId = post.getParentId();
        }

        boolean isVideo = post.getDuration() > 0;
        this.previousPostId = this.postId;
        this.postId = postId;
        this.inList = inList;

        //Same gallery
        if (this.galleryId != null && this.galleryId.equals(galleryId)) {
            segmentManager.postSession(postId, previousPostId, galleryId, inList, postSessionDurationDifference,
                    isVideo, post.getDuration(), postMuted);
        }
        else {
            segmentManager.postSession(postId, null, galleryId, inList, postSessionDurationDifference,
                    isVideo, post.getDuration(), postMuted);
        }
        this.galleryId = galleryId;
    }

    public void postSessionMute(boolean muted) {
        this.postMuted = muted;
    }

    public void stopTrackingPost() {
        //Will check and update a post session based off of the current postId and galleryId in the manager.
        String galleryId = this.galleryId;
        //the logic of updating a post session to start another post session vs. ending a post session are different.
        if (postSessionDuration == 0 || this.postId == null) {
            return;
        }
        long postSessionDurationDifference = (System.currentTimeMillis() - postSessionDuration) / 1000L;

        //Get the post
        Post post = SQLite.select()
                          .from(Post.class)
                          .where(Post_Table.id.eq(this.postId))
                          .querySingle();
        if (post == null) {
            return;
        }

        if (StringUtils.toNullIfEmpty(galleryId) == null) {
            galleryId = post.getParentId();
        }

        boolean isVideo = post.getDuration() > 0;

        //Same gallery
        if (this.galleryId != null && galleryId != null && this.galleryId.equals(galleryId)) {
            segmentManager.postSession(this.postId, this.previousPostId, galleryId, inList, postSessionDurationDifference,
                    isVideo, post.getDuration(), postMuted);
        }
        else {
            segmentManager.postSession(postId, null, galleryId, inList, postSessionDurationDifference,
                    isVideo, post.getDuration(), postMuted);
        }
        this.postId = null;
        this.previousPostId = null;
        this.galleryId = null;
        postSessionDuration = 0;
    }

    //-------------------------------------STORIES------------------------------------------//
    public void articleOpened(String articleId) {
        //User taps on Article underneath Gallery
        segmentManager.articleOpened(articleId);
    }

    public void articleOpened(String articleId, String articleUrl) {
        //User taps on Article underneath Gallery
        segmentManager.articleOpened(articleId, articleUrl);
    }

    public void storiesScreenOpened() {
        storiesScreenSessionDuration = System.currentTimeMillis();
        browsingStories = true;
    }

    public void storiesScreenClosed() {
        // The amount of time a user spends browsing Stories in a session.
        //A session begins when opening the Stories tab and ends when the user navigates to another screen.
        if (browsingStories) {
            browsingStories = false;
            storiesScreenSessionDuration = (System.currentTimeMillis() - storiesScreenSessionDuration) / 1000L;
            segmentManager.storiesScreenClosed(storiesScreenSessionDuration, totalStoriesScrolledBy);
//            segmentManager.storiesScrolledBy(totalStoriesScrolledBy);
            totalStoriesScrolledBy = 0;
        }
    }

    public void storiesScrolledBy(int storiesScrolledBy) {
        //The number of stories a user scrolls past in a session.
        //A session begins when opening the Highlights tab and ends when the user closes the app.
        this.totalStoriesScrolledBy += storiesScrolledBy;
    }

    public void galleriesOpenedFromStories(String galleryId) {
        //User taps “Read More” when browsing the Stories tab.
        openedFrom = "stories";
        segmentManager.galleriesOpenedFromStories(galleryId);
    }

    public void gallerySharedFromStories(String galleryId) {
        //User taps the share icon when browsing the Stories tab.
        segmentManager.gallerySharedFromStories(galleryId);
    }

    //-------------------------------------PROFILE------------------------------------------//
    public void profileScreenOpened() {
        profileScreenSessionDuration = System.currentTimeMillis();
        browsingProfile = true;
    }

    public void profileScreenClosed(String userId) {
        if (browsingProfile) {
            browsingProfile = false;
            profileScreenSessionDuration = (System.currentTimeMillis() - profileScreenSessionDuration) / 1000L;
            segmentManager.profileClosed(profileScreenSessionDuration, totalGalleriesScrolledBy, userId);
            totalGalleriesScrolledBy = 0;
        }
    }

    public void galleriesScrolledByInProfile(int galleriesScrolledBy) {
        //The number of stories a user scrolls past in a session.
        //A session begins when opening the Highlights tab and ends when the user closes the app.
        this.totalGalleriesScrolledBy += galleriesScrolledBy; //This is sent to mix panel when you leave highlights
    }

    public void galleriesOpenedFromProfile(String galleryId, String userId) {
        //User taps “Read More” when browsing the Stories tab.
        openedFrom = "profile";
        segmentManager.galleriesOpenedFromProfile(galleryId, userId);
    }

    public void gallerySharedFromProfile(String galleryId, String userId) {
        //User taps the share icon when browsing the Stories tab.
        segmentManager.gallerySharedFromProfile(galleryId, userId);
    }

    //--------------------------------------------PERMISSIONS-----------------------------------//
    public void checkPermissionChanges() {
        LogUtils.i(TAG, "check permissions");
        SharedPreferences sharedPreferences = context.getSharedPreferences(ANALYTICS_PERMISSIONS, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        boolean locationPermission = sharedPreferences.getBoolean(LOCATION_PERMISSION, false);
        boolean notificationPermission = sharedPreferences.getBoolean(NOTIFICATION_PERMISSION, false);
        boolean cameraPermission = sharedPreferences.getBoolean(CAMERA_PERMISSION, false);
        boolean micPermission = sharedPreferences.getBoolean(MIC_PERMISSION, false);
        boolean photoPermission = sharedPreferences.getBoolean(PHOTO_PERMISSION, false);

        int res = 0;
        boolean granted = false;
        res = context.checkCallingOrSelfPermission(LOCATION_PERMISSION);
        granted = (res == PackageManager.PERMISSION_GRANTED);
        if (granted != locationPermission) {
            if (granted) {
                segmentManager.permissionLocationEnabled();
            }
            else {
                segmentManager.permissionLocationDisabled();
            }
            editor.putBoolean(LOCATION_PERMISSION, !locationPermission);
        }

//        res = context.checkCallingOrSelfPermission(NOTIFICATION_PERMISSION);
//        granted = (res == PackageManager.PERMISSION_GRANTED);
//        if(granted!=notificationPermission) {
////        if(granted){
////            segmentManager.permissionNotificationEnabled();
////            Log.i(TAG, "notif enabled");
////
////        } else{
////            Log.i(TAG, "notif disabled");
////            segmentManager.permissionNotificationDisabled();
////        }
//                    editor.putBoolean(NOTIFICATION_PERMISSION, !notificationPermission);
//        }

        res = context.checkCallingOrSelfPermission(CAMERA_PERMISSION);
        granted = (res == PackageManager.PERMISSION_GRANTED);
        if (granted != cameraPermission) {
            if (granted) {
                segmentManager.permissionCameraEnabled();
            }
            else {
                segmentManager.permissionCameraDisabled();
            }
            editor.putBoolean(CAMERA_PERMISSION, !cameraPermission);
        }

        res = context.checkCallingOrSelfPermission(MIC_PERMISSION);
        granted = (res == PackageManager.PERMISSION_GRANTED);
        if (granted != micPermission) {
            if (granted) {
                segmentManager.permissionMicrophoneEnabled();
            }
            else {
                segmentManager.permissionMicrophoneDisabled();
            }
            editor.putBoolean(MIC_PERMISSION, !micPermission);
        }

        res = context.checkCallingOrSelfPermission(PHOTO_PERMISSION);
        granted = (res == PackageManager.PERMISSION_GRANTED);
        if (granted != photoPermission) {
            if (res == PackageManager.PERMISSION_GRANTED) {
                segmentManager.permissionPhotosEnabled();
            }
            else {
                segmentManager.permissionLocationDisabled();
            }
            editor.putBoolean(PHOTO_PERMISSION, !photoPermission);
        }
        editor.apply();
    }

}

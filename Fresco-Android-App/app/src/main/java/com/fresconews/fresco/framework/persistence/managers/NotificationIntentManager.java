package com.fresconews.fresco.framework.persistence.managers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.v2.assignments.AssignmentMapActivity;
import com.fresconews.fresco.v2.assignments.GlobalAssignmentActivity;
import com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailActivity;
import com.fresconews.fresco.v2.gallery.gallerylist.GalleryListActivity;
import com.fresconews.fresco.v2.home.HomeActivity;
import com.fresconews.fresco.v2.notifications.NotificationType;
import com.fresconews.fresco.v2.profile.ProfileActivity;
import com.fresconews.fresco.v2.settings.SettingsActivity;
import com.fresconews.fresco.v2.storiespreview.StoriesPreviewActivity;
import com.fresconews.fresco.v2.storygallery.StoryGalleryActivity;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Maurice on 01/09/2016.
 */
public class NotificationIntentManager {
    private static final String TAG = NotificationIntentManager.class.getSimpleName();

    public static final String TYPE = "type";
    public static final String TITLE = "title";
    public static final String BODY = "body";

    public static final String IMAGE = "image";
    public static final String USER_ID = "user_id";
    public static final String GALLERY_ID = "gallery_id";
    public static final String GALLERY_IDS = "gallery_ids";
    public static final String ASSIGNMENT_ID = "assignment_id";
    public static final String IS_GLOBAL = "is_global";
    public static final String STORY_ID = "story_id";
    public static final String USER_IDS = "user_ids";
    public static final String COMMENT_IDS = "comment_ids";
    public static final String POST_ID = "post_id";

    public static final String FROM_PUSH = "fromPush";
    public static final String PUSH_KEY = "pushKey";
    public static final String OBJECT_ID = "objetId";
    public static final String OBJECT_TYPE = "objectType";
    public static final String EXPIRED_ASSIGNMENT = "expiredAssignment";

    private AnalyticsManager analyticsManager;
    private AssignmentManager assignmentManager;
    private FrescoLocationManager frescoLocationManager;
    private UserManager userManager;
    private PaymentManager paymentManager;

    public NotificationIntentManager(AnalyticsManager analyticsManager, AssignmentManager assignmentManager, FrescoLocationManager frescoLocationManager, UserManager userManager, PaymentManager paymentManager) {
        this.analyticsManager = analyticsManager;
        this.assignmentManager = assignmentManager;
        this.frescoLocationManager = frescoLocationManager;
        this.userManager = userManager;
        this.paymentManager = paymentManager;
        ((Fresco2) Fresco2.getContext().getApplicationContext()).getFrescoComponent().inject(this);
    }

    /**
     * Goes through every type until finding the correct type and create the intent to launch
     *
     * @param context The context needed to create the intent
     * @param data    The data containing all the ids required to launch the intent
     * @return The intent to launch
     */
    public Observable<Intent> findIntent(Context context, Map<String, String> data, boolean isSecondaryAction, boolean fromFeed) {
        Intent intent = null;
        if (data != null) {
            String type = data.get(TYPE);
            String title = data.get(TITLE);
            String object = "";
            String id = "";
            //object e.g. assignment, gallery, user — Type of object being sent in the meta, only applicable when it's a single object

            if (!TextUtils.isEmpty(type)) {
                switch (type) {
                    // News
                    case NotificationType.NEWS_PHOTOS_OF_THE_DAY:
                        break;
                    case NotificationType.NEWS_TODAY_IN_NEWS:
                        if (data.containsKey(GALLERY_IDS)) {
                            intent = viewGalleries(context, data.get(GALLERY_IDS), title);
                        }
                        break;
                    case NotificationType.NEWS_GALLERY:
                        if (data.containsKey(GALLERY_ID)) {
                            id = data.get(GALLERY_ID);
                            object = "gallery";
                            intent = viewGallery(context, id);
                        }
                        break;
                    case NotificationType.NEWS_STORY:
                        if (data.containsKey(STORY_ID)) {
                            id = data.get(STORY_ID);
                            object = "story";
                            intent = viewStory(context, id);
                        }
                        break;
                    case NotificationType.NEWS_CUSTOM:
                        break;
                    // Assignments
                    case NotificationType.DISPATCH_NEW_ASSIGNMENT:
                        if (data.containsKey(ASSIGNMENT_ID)) {
                            String assignmentId = data.get(ASSIGNMENT_ID);
                            id = data.get(ASSIGNMENT_ID);
                            object = "assignment";

                            if (!TextUtils.isEmpty(assignmentId)) {
                                if (!isSecondaryAction && !fromFeed) {
                                    analyticsManager.notificationReceived(type, id, object);
                                }
                                return assignmentManager.downloadAssignment(assignmentId)
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .map(assignment -> {
                                                            Intent mapIntent = new Intent();
                                                            mapIntent.putExtra(FROM_PUSH, !fromFeed);
                                                            mapIntent.putExtra(PUSH_KEY, type);
                                                            mapIntent.putExtra(OBJECT_ID, data.get(ASSIGNMENT_ID));
                                                            mapIntent.putExtra(OBJECT_TYPE, "assignment");
                                                            if (assignment != null) {
                                                                if (assignment.getEndsAt().before(new Date())) { //if expired
                                                                    mapIntent.putExtra(EXPIRED_ASSIGNMENT, true);
                                                                    if(fromFeed) {
                                                                        return mapIntent;
                                                                    }
                                                                }
                                                                if (isSecondaryAction) {
                                                                    mapIntent = viewAssignmentOnGoogleMaps(assignment.getAddress(), frescoLocationManager.getUserLocation());
                                                                    mapIntent.putExtra(FROM_PUSH, !fromFeed);
                                                                    mapIntent.putExtra(PUSH_KEY, type);
                                                                    mapIntent.putExtra(OBJECT_ID, data.get(ASSIGNMENT_ID));
                                                                    mapIntent.putExtra(OBJECT_TYPE, "assignment");
                                                                    return mapIntent;
                                                                }
                                                                else {
                                                                    if (assignment.isGlobal()) {
                                                                        mapIntent = viewGlobalAssignment(context);
                                                                        mapIntent.putExtra(FROM_PUSH, !fromFeed);
                                                                        mapIntent.putExtra(PUSH_KEY, type);
                                                                        mapIntent.putExtra(OBJECT_ID, data.get(ASSIGNMENT_ID));
                                                                        mapIntent.putExtra(OBJECT_TYPE, "assignment");
                                                                        if (assignment.getEndsAt().before(new Date())) { //if expired
                                                                            mapIntent.putExtra(EXPIRED_ASSIGNMENT, true);
                                                                        }
                                                                        return mapIntent;
                                                                    }
                                                                    mapIntent = viewAssignment(context, assignmentId);
                                                                    mapIntent.putExtra(FROM_PUSH, !fromFeed);
                                                                    mapIntent.putExtra(PUSH_KEY, type);
                                                                    mapIntent.putExtra(OBJECT_ID, data.get(ASSIGNMENT_ID));
                                                                    mapIntent.putExtra(OBJECT_TYPE, "assignment");
                                                                    return mapIntent;
                                                                }
                                                            }
                                                            return mapIntent;
                                                        });
                            }
                        }
                        break;
                    case NotificationType.DISPATCH_ASSIGMENT_EXPIRED:
                        // TODO
                        break;
                    // Social
                    case NotificationType.SOCIAL_FOLLOWED:
                        if (data.containsKey(USER_IDS)) {
                            String ids = data.get(USER_IDS);
                            if (!TextUtils.isEmpty(ids)) {
                                ids = ids.replace("[", "").replace("]", "").replace("\"", "");
                                String[] array = ids.split(",");
                                if (array.length > 0) {
                                    String userId = array[0];
                                    id = array[0];
                                    object = "user";
                                    if (isSecondaryAction) {
                                        userManager.getUser(userId)
                                                   .onErrorReturn(throwable -> null)
                                                   .subscribe(user -> {
                                                       if (user != null) {
                                                           userManager.follow(user)
                                                                      .onErrorReturn(throwable -> null)
                                                                      .subscribe();
                                                       }
                                                   });
                                    }
                                    else {
                                        intent = viewProfile(context, userId, isSecondaryAction);
                                    }
                                }
                            }
                        }
                        break;
                    case NotificationType.SOCIAL_LIKED:
                    case NotificationType.SOCIAL_REPOSTED:
                        if (data.containsKey(GALLERY_ID)) {
                            id = data.get(GALLERY_ID);
                            object = "gallery";
                            String galleryId = id;
                            if (!TextUtils.isEmpty(galleryId)) {
                                intent = viewGallery(context, galleryId);
                            }
                        }
                        break;
                    case NotificationType.SOCIAL_COMMENTED:
                    case NotificationType.SOCIAL_MENTIONED:
                        if (data.containsKey(GALLERY_ID) && data.containsKey(COMMENT_IDS)) {
                            String ids = data.get(COMMENT_IDS);
                            String galleryId = data.get(GALLERY_ID);
                            if (!TextUtils.isEmpty(ids) && !TextUtils.isEmpty(galleryId)) {
                                ids = ids.replace("[", "").replace("]", "").replace("\"", "");
                                String[] array = ids.split(",");
                                if (array.length > 0) {
                                    String commentId = array[0];
                                    id = array[0];
                                    object = "comment";
                                    intent = viewGalleryComment(context, commentId, galleryId);
                                }
                            }
                        }
                        break;
                    // Payment
                    case NotificationType.DISPATCH_PURCHASED:
                        if (data.containsKey(GALLERY_ID)) {
                            id = data.get(GALLERY_ID);
                            object = "gallery";
                            String galleryId = data.get(GALLERY_ID);
                            if (!TextUtils.isEmpty(galleryId)) {
                                if (!isSecondaryAction && !fromFeed) {
                                    analyticsManager.notificationReceived(type, id, object);
                                }

                                return paymentManager.getPayments()
                                                     .subscribeOn(Schedulers.io())
                                                     .observeOn(AndroidSchedulers.mainThread())
                                                     .onErrorReturn(throwable -> null)
                                                     .map(networkCreditCards -> {
                                                         Intent paymentIntent = new Intent();
                                                         paymentIntent.putExtra(FROM_PUSH, true);
                                                         paymentIntent.putExtra(PUSH_KEY, type);
                                                         paymentIntent.putExtra(OBJECT_ID, data.get(GALLERY_ID));
                                                         paymentIntent.putExtra(OBJECT_TYPE, "gallery");

                                                         if (networkCreditCards == null || networkCreditCards.isEmpty()) {
                                                             paymentIntent = addPaymentInfo(context);
                                                             paymentIntent.putExtra(FROM_PUSH, true);
                                                             paymentIntent.putExtra(PUSH_KEY, type);
                                                             paymentIntent.putExtra(OBJECT_ID, data.get(GALLERY_ID));
                                                             paymentIntent.putExtra(OBJECT_TYPE, "gallery");
                                                             return paymentIntent;
                                                         }
                                                         if (data.containsKey(POST_ID) && !TextUtils.isEmpty(data.get(POST_ID))) {
                                                             paymentIntent = viewGallery(context, galleryId, data.get(POST_ID));
                                                             paymentIntent.putExtra(FROM_PUSH, true);
                                                             paymentIntent.putExtra(PUSH_KEY, type);
                                                             paymentIntent.putExtra(OBJECT_ID, data.get(GALLERY_ID));
                                                             paymentIntent.putExtra(OBJECT_TYPE, "gallery");
                                                             return paymentIntent;
                                                         }
                                                         paymentIntent = viewGallery(context, galleryId);
                                                         paymentIntent.putExtra(FROM_PUSH, true);
                                                         paymentIntent.putExtra(PUSH_KEY, type);
                                                         paymentIntent.putExtra(OBJECT_ID, data.get(GALLERY_ID));
                                                         paymentIntent.putExtra(OBJECT_TYPE, "gallery");
                                                         return paymentIntent;
                                                     });
                            }
                        }
                        break;
                    case NotificationType.DISPATCH_CONTENT_VERIFIED:
                        if (data.containsKey(GALLERY_ID)) {
                            id = data.get(GALLERY_ID);
                            object = "gallery";
                            String galleryId = data.get(GALLERY_ID);
                            if (!TextUtils.isEmpty(galleryId)) {
                                intent = viewGallery(context, galleryId);
                            }
                        }
                        break;
                    case NotificationType.PAYMENT_PAYMENT_EXPIRING:
                    case NotificationType.PAYMENT_PAYMENT_DECLINED:
                        intent = addPaymentInfo(context);
                        break;
                    case NotificationType.PAYMENT_PAYMENT_SENT:
                        break;
                    case NotificationType.PAYMENT_TAX_INFO_REQUIRED:
                    case NotificationType.PAYMENT_TAX_INFO_DECLINED:
                        intent = addTaxInfo(context);
                        break;
                    case NotificationType.PAYMENT_TAX_INFO_PROCESSED:
                        break;
                }
            }

            if (intent == null) {
                intent = new Intent();
            }

            intent.putExtra(FROM_PUSH, true);
            intent.putExtra(PUSH_KEY, type);
            intent.putExtra(OBJECT_ID, id);
            intent.putExtra(OBJECT_TYPE, object);

            //object e.g. assignment, gallery, user — Type of object being sent in the meta, only applicable when it's a single object
            if (!isSecondaryAction && !fromFeed) {
                analyticsManager.notificationReceived(type, id, object);
            }

            return Observable.just(intent);
        }

        return null;
    }

    private Intent viewGalleries(Context context, String ids, String title) {
        ArrayList<String> galleryIds = new ArrayList<>();
        String[] idsSplit = ids.replace("[", "").replace("]", "").replace("\"", "").split(",");
        for (int i = 0; i < idsSplit.length; i++) {
            galleryIds.add(idsSplit[i]);
        }
        if (galleryIds.isEmpty()) {
            return null;
        }
        Intent intent = new Intent(context, GalleryListActivity.class);
        intent.putExtra(GalleryListActivity.GALLERY_IDS, galleryIds);
        intent.putExtra(GalleryListActivity.TITLE, title);

        return intent;
    }

    private Intent viewGallery(Context context, String id) {
        return viewGallery(context, id, null);
    }

    private Intent viewGallery(Context context, String id, String postId) {
        //Download and save gallery

        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(HomeActivity.EXTRA_ACTION_FROM_NOTIFICATION, true);
        intent.putExtra(GalleryDetailActivity.EXTRA_GALLERY_ID, id);
        if (!TextUtils.isEmpty(postId)) {
            intent.putExtra(GalleryDetailActivity.EXTRA_CURRENT_POST, postId);
        }

        return intent;
    }

    private Intent viewStory(Context context, String id) {
        Intent intent = new Intent(context, StoriesPreviewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(StoriesPreviewActivity.EXTRA_ACTION_FROM_NOTIFICATION, true);
        intent.putExtra(StoryGalleryActivity.EXTRA_STORY_TITLE, "");
        intent.putExtra(StoryGalleryActivity.EXTRA_STORY_ID, id);
        intent.putExtra(StoryGalleryActivity.EXTRA_STORY_CAPTION, "");

        return intent;
    }

    private Intent viewAssignment(Context context, String id) {
        Intent intent = new Intent(context, AssignmentMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(AssignmentMapActivity.EXTRA_ASSIGNMENT_ID, id);

        return intent;
    }

    private Intent viewGlobalAssignment(Context context) {
        return new Intent(context, GlobalAssignmentActivity.class);
    }

    private Intent viewAssignmentOnGoogleMaps(String address, LatLng userLoc) {
        String url;
        if (userLoc == null) {
            url = String.format(Locale.getDefault(), "https://www.google.com/maps/dir/%s/", address);
        }
        else {
            url = String.format(Locale.getDefault(), "https://www.google.com/maps/dir/%f,+%f/%s/", userLoc.latitude, userLoc.longitude, address);
        }

        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }

    private Intent viewProfile(Context context, String userId, boolean followBack) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ProfileActivity.EXTRA_USER_ID, userId);
        intent.putExtra(ProfileActivity.EXTRA_FOLLOW_BACK, followBack);

        return intent;
    }

    private Intent viewProfileGallery(Context context, String userId, String galleryId) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ProfileActivity.FROM_PUSH_NOTIFICATION, true);
        intent.putExtra(ProfileActivity.EXTRA_USER_ID, userId);
        intent.putExtra(GalleryDetailActivity.EXTRA_GALLERY_ID, galleryId);

        return intent;
    }

    private Intent viewGalleryComment(Context context, String commentId, String galleryId) {
        Intent intent = viewGallery(context, galleryId);
        intent.putExtra(GalleryDetailActivity.EXTRA_CURRENT_COMMENT_ID, commentId);

        return intent;
    }

    private Intent addPaymentInfo(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_PAYMENT_INFO, true);

        return intent;
    }

    private Intent addTaxInfo(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_TAX_INFO, true);

        return intent;
    }

    private Intent addStateIdInfo(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(SettingsActivity.EXTRA_SHOW_STATE_ID_INFO, true);

        return intent;
    }
}

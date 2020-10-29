package com.fresconews.fresco.framework.mvvm.viewmodels;

import android.app.Activity;
import android.content.Intent;
import android.databinding.Bindable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.mvvm.ItemViewModel;
import com.fresconews.fresco.framework.persistence.managers.NotificationIntentManager;
import com.fresconews.fresco.framework.persistence.models.Notification;
import com.fresconews.fresco.v2.notifications.NotificationType;
import com.fresconews.fresco.v2.utils.DialogUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import rx.functions.Action1;

/**
 * Created by Maurice on 30/08/2016.
 */
public class NotificationFeedItemViewModel extends ItemViewModel<Notification> {

    @Inject
    NotificationIntentManager notificationIntentManager;

    private WeakReference<Activity> activity;
    private boolean shouldHaveAvatar;
    public static final String EXPIRED_ASSIGNMENT = "expiredAssignment";
    public static final String FROM_PUSH = "fromPush";

    public NotificationFeedItemViewModel(Activity activity, Notification item) {
        super(item);

        this.activity = new WeakReference<>(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        shouldHaveAvatar = getType().contains("social");
    }

    public String getId() {
        return getItem().getId();
    }

    @Bindable
    public boolean isSeen() {
        return getItem().isSeen();
    }

    @Bindable
    public String getTitle() {
        return getItem().getTitle();
    }

    @Bindable
    public String getBody() {
        return getItem().getBody();
    }

    @Bindable
    public String getImage() {
        return getItem().getImage();
    }

    @Bindable
    public int getUserCount() {
        if (TextUtils.isEmpty(getItem().getUserIds())) {
            return 0;
        }
        else {
            return getItem().getUserIds().split(",").length;
        }
    }

    @Bindable
    public String getUserCountString() {
        return String.format(Locale.getDefault(), "+%d", getUserCount() - 1);
    }

    @Bindable
    public Drawable getSecondaryActionImage() {
        switch (getItem().getType()) {
            case NotificationType.DISPATCH_NEW_ASSIGNMENT:
                if (!getItem().isGlobal()) {
                    return ActivityCompat.getDrawable(activity.get(), R.drawable.directions);
                }
                break;
        }
        return null;
    }

    @Bindable
    public boolean isShouldHaveAvatar() {
        return shouldHaveAvatar;
    }

    private String getType() {
        return getItem().getType();
    }

    private Map<String, String> createData() {
        Map<String, String> data = new HashMap<>();
        data.put(NotificationIntentManager.TITLE, getItem().getTitle());
        data.put(NotificationIntentManager.TYPE, getItem().getType());
        data.put(NotificationIntentManager.GALLERY_ID, getItem().getGalleryId());
        data.put(NotificationIntentManager.STORY_ID, getItem().getStoryId());
        data.put(NotificationIntentManager.ASSIGNMENT_ID, getItem().getAssignmentId());
        data.put(NotificationIntentManager.USER_ID, getItem().getUserId());
        data.put(NotificationIntentManager.POST_ID, getItem().getPostId());

        if (!TextUtils.isEmpty(getItem().getUserIds())) {
            String userArrayString = "[\"" + getItem().getUserIds() + "\"]";
            try {
                JSONArray userArray = new JSONArray(userArrayString);
                data.put(NotificationIntentManager.USER_IDS, userArray.toString());
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(getItem().getGalleryIds())) {
            String galleryArrayString = "[\"" + getItem().getGalleryIds() + "\"]";
            try {
                JSONArray galleryArray = new JSONArray(galleryArrayString);
                data.put(NotificationIntentManager.GALLERY_IDS, galleryArray.toString());
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(getItem().getCommentIds())) {
            String commentArrayString = "[\"" + getItem().getCommentIds() + "\"]";
            try {
                JSONArray commentArray = new JSONArray(commentArrayString);
                data.put(NotificationIntentManager.COMMENT_IDS, commentArray.toString());
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    private void handleIntent(Intent intent) {
        boolean expiredAssignment = false;
        boolean fromPush = false;

        if (intent != null) {
            expiredAssignment = intent.getBooleanExtra(EXPIRED_ASSIGNMENT, false);
            fromPush = intent.getBooleanExtra(FROM_PUSH, false);
        }

        if (intent != null && (intent.getComponent() != null || intent.getData() != null) && !expiredAssignment) {
            activity.get().finish();
            activity.get().startActivity(intent);
        }
        else {
            String message = "";
            switch (getType()) {
                case NotificationType.NEWS_TODAY_IN_NEWS:
                    message = "galleries";
                    break;
                case NotificationType.NEWS_GALLERY:
                case NotificationType.SOCIAL_LIKED:
                case NotificationType.SOCIAL_REPOSTED:
                case NotificationType.SOCIAL_COMMENTED:
                case NotificationType.SOCIAL_MENTIONED:
                case NotificationType.DISPATCH_PURCHASED:
                case NotificationType.DISPATCH_CONTENT_VERIFIED:
                    message = "gallery";
                    break;
                case NotificationType.NEWS_STORY:
                    message = "story";
                    break;
                case NotificationType.DISPATCH_NEW_ASSIGNMENT:
                    message = "assignment";
                    break;
                case NotificationType.SOCIAL_FOLLOWED:
                    message = "user";
                    break;
            }
            if (!TextUtils.isEmpty(message)) {

                if (getType().equals(NotificationType.DISPATCH_ASSIGMENT_EXPIRED) || (expiredAssignment && !fromPush)) {
                    DialogUtils.showFrescoDialog(activity.get(), R.string.expired, R.string.error_assignment_expired, R.string.dismiss,
                            (dialogInterface, i) -> dialogInterface.dismiss());
                }
                else {
                    DialogUtils.showFrescoDialog(activity.get(), activity.get().getString(R.string.error), activity.get().getString(R.string.error_loading_notification, message), R.string.dismiss,
                            (dialogInterface, i) -> dialogInterface.dismiss());
                }
            }
        }
    }

    public Action1<View> goToActivity = view -> {
        Map<String, String> data = createData();
        notificationIntentManager.findIntent(activity.get(), data, false, true)
                                 .onErrorReturn(throwable -> null)
                                 .subscribe(this::handleIntent);
    };

    public Action1<View> goToSecondaryActivity = view -> {
        Map<String, String> data = createData();

        notificationIntentManager.findIntent(activity.get(), data, true, true)
                                 .onErrorReturn(throwable -> null)
                                 .subscribe(this::handleIntent);
    };
}

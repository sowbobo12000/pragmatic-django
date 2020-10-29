package com.fresconews.fresco.framework.mvvm.viewmodels;

import android.app.Activity;
import android.content.Intent;
import android.databinding.Bindable;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.framework.mvvm.ItemViewModel;
import com.fresconews.fresco.framework.network.EndpointHelper;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.StoryManager;
import com.fresconews.fresco.framework.persistence.models.Story;
import com.fresconews.fresco.v2.onboarding.OnboardingActivity;
import com.fresconews.fresco.v2.storygallery.StoryGalleryActivity;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.ref.WeakReference;
import java.util.Date;

import javax.inject.Inject;

import rx.functions.Action1;

/**
 * Created by techjini on 5/7/16.
 */
public class StoriesPreviewItemModel extends ItemViewModel<Story> {

    private WeakReference<Activity> activity;

    @Inject
    StoryManager storyManager;

    @Inject
    SessionManager sessionManager;

    @Inject
    AnalyticsManager analyticsManager;

    private boolean showReposted = false;

    public StoriesPreviewItemModel(Activity paramActivity, Story paramItem) {
        super(paramItem);
        activity = new WeakReference<>(paramActivity);
        ((Fresco2) paramActivity.getApplication()).getFrescoComponent().inject(this);
    }

    public StoriesPreviewItemModel(Activity activity, Story item, boolean showReposted) {
        super(item);
        this.activity = new WeakReference<>(activity);
        this.showReposted = showReposted;
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);
    }

    public String getId() {
        if (getItem() == null) {
            return null;
        }
        return getItem().getId();
    }

    @Bindable
    public String getTitle() {
        if (getItem() == null) {
            return null;
        }
        return getItem().getTitle();
    }

    @Bindable
    public String getCaption() {
        if (getItem() == null) {
            return null;
        }
        return getItem().getCaption();
    }

    @Bindable
    public boolean isCaptionFilled() {
        if (getCaption() == null) {
            return false;
        }
        return !getCaption().equals("");
    }

    @Bindable
    public int getThumbnailsSize() {
        if (getItem() == null) {
            return 0;
        }
        return getItem().loadThumbnails().size();
    }

    @Bindable
    public String getImage1() {
        return getImage(0);
    }

    @Bindable
    public String getImage2() {
        return getImage(1);
    }

    @Bindable
    public String getImage3() {
        return getImage(2);
    }

    @Bindable
    public String getImage4() {
        return getImage(3);
    }

    @Bindable
    public String getImage5() {
        return getImage(4);
    }

    @Bindable
    public String getImage6() {
        return getImage(5);
    }

    private String getImage(int index) {
        if (index >= getThumbnailsSize()) {
            return "";
        }
        return getItem().loadThumbnails().get(index).getImage();
    }

    @Bindable
    public int getLikes() {
        return getItem().getLikes();
    }

    @Bindable
    public int getReposts() {
        if (getItem() == null) {
            return 0;
        }
        return getItem().getReposts();
    }

    @Bindable
    public boolean isLiked() {
        return getItem() != null && getItem().isLiked();
    }

    @Bindable
    public boolean isReposted() {
        return getItem() != null && getItem().isReposted();
    }

    @Bindable
    public String getRepostedBy() {
        if (!showReposted || getItem().getRepostedBy() == null) {
            return "";
        }
        return getItem().getRepostedBy().toUpperCase();
    }

    @Bindable
    public String getTime() {
        Date date = getItem().getUpdatedAt() == null ? getItem().getCreatedAt() : getItem().getUpdatedAt();
        DateTime dateTime = new DateTime(date);
        DateTimeFormatter fmt = DateTimeFormat.shortDateTime();
        return fmt.print(dateTime);
    }

    @Bindable
    public String getAddress() {
        return getItem().getAddress();
    }

    public Action1<View> readMore = view -> {
        StoryGalleryActivity.start(view.getContext(), getItem().getId(), getItem().getTitle(), getCaption());
    };

    public Action1<View> share = view -> {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, EndpointHelper.currentEndpoint.frescoWebsite + "story/" + getId());

        activity.get().startActivity(Intent.createChooser(intent, "Share Story"));
    };

    public Action1<View> like = view -> {
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(activity.get(), OnboardingActivity.class);
            activity.get().startActivity(intent);
            return;
        }

        if (isLiked()) {
            storyManager.unlike(getItem())
                        .onErrorReturn(throwable -> null)
                        .subscribe(success -> updateLikes());
        }
        else {
            storyManager.like(getItem())
                        .onErrorReturn(throwable -> null)
                        .subscribe(success -> updateLikes());
        }
        updateLikes();
    };

    public Action1<View> repost = view -> {
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(activity.get(), OnboardingActivity.class);
            activity.get().startActivity(intent);
            return;
        }

        if (isReposted()) {
            storyManager.unrepost(getItem())
                        .onErrorReturn(throwable -> null)
                        .subscribe(success -> updateReposts());
        }
        else {
            storyManager.repost(getItem())
                        .onErrorReturn(throwable -> null)
                        .subscribe(success -> updateReposts());
        }
        updateReposts();
    };

    private void updateLikes() {
        notifyPropertyChanged(BR.likes);
        notifyPropertyChanged(BR.liked);
    }

    private void updateReposts() {
        notifyPropertyChanged(BR.reposts);
        notifyPropertyChanged(BR.reposted);
    }
}

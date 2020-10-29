package com.fresconews.fresco.framework.mvvm.viewmodels;

import android.app.Activity;
import android.content.Intent;
import android.databinding.Bindable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ItemViewModel;
import com.fresconews.fresco.framework.network.EndpointHelper;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.Gallery;
import com.fresconews.fresco.framework.persistence.models.Post;
import com.fresconews.fresco.v2.follow.FollowActivity;
import com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailActivity;
import com.fresconews.fresco.v2.gallery.gallerylist.GalleryListActivity;
import com.fresconews.fresco.v2.home.HomeActivity;
import com.fresconews.fresco.v2.onboarding.OnboardingActivity;
import com.fresconews.fresco.v2.profile.ProfileActivity;
import com.fresconews.fresco.v2.search.SearchActivity;
import com.fresconews.fresco.v2.storygallery.StoryGalleryActivity;
import com.fresconews.fresco.v2.gallery.PostPagerAdapter;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.viewpagerindicator.CirclePageIndicator;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import rx.functions.Action1;

public class GalleryViewModel extends ItemViewModel<Gallery> {
    public static final String TAG = GalleryViewModel.class.getSimpleName();

    @Inject
    GalleryManager galleryManager;

    @Inject
    SessionManager sessionManager;

    @Inject
    AnalyticsManager analyticsManager;

    @Inject
    UserManager userManager;

    public BindableView<View> topView = new BindableView<>();
    public BindableView<ViewPager> viewPager = new BindableView<>();
    public BindableView<CirclePageIndicator> circleIndicator = new BindableView<>();

    private static final int GALLERY_LIKES_STATE = 11;
    private static final int GALLERY_REPOSTS_STATE = 12;

    private WeakReference<Activity> activity;
    private List<Post> posts;

    private boolean showReposted;

    public GalleryViewModel(Activity activity, Gallery item) {
        super(item);
        this.activity = new WeakReference<>(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);
    }

    public GalleryViewModel(Activity activity, Gallery item, boolean showReposted) {
        super(item);
        this.activity = new WeakReference<>(activity);
        this.showReposted = showReposted;
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }

        super.onBound();

        if (viewPager.get() == null || circleIndicator.get() == null) {
            return;
        }

        posts = galleryManager.getPostsForGallery(getId());

        if (posts != null && posts.size() != 0 && posts.get(0) != null) {
            topView.get().setTag(posts.get(0).getId());
        }

        PostPagerAdapter adapter = new PostPagerAdapter(activity.get(), posts);
        viewPager.get().setAdapter(adapter);
        circleIndicator.get().setViewPager(viewPager.get());

        // This is used to keep all of the video views in memory, to make sure they can continue to play
        // TODO: 8/19/2016 Figure out a better way of allowing the video to continue to play after leaving memory
        viewPager.get().setOffscreenPageLimit(8);

        viewPager.get().clearOnPageChangeListeners();
        viewPager.get().addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position >= posts.size()) {
                    return;
                }
                String postId = posts.get(position).getId();
                topView.get().setTag(postId);
                viewPager.get().post(() -> Fresco2.setActivePostIdInViewAnalytics(postId, getItem().getId(), true));
            }
        });

        // needed because ViewPagerIndicator does not respect the android:gravity xml parameter
        // and we want the indicator to be right aligned
        int indicatorRadius = Fresco2.getContext().getResources()
                                     .getDimensionPixelSize(R.dimen.page_indicator_radius);
        circleIndicator.get().getLayoutParams().width = adapter.getCount() * indicatorRadius * 3;
    }

    public String getId() {
        if (getItem() == null) {
            return null;
        }
        return getItem().getId();
    }

    public Date getActionAt() {
        if (getItem() == null) {
            return null;
        }
        return getItem().getActionAt();
    }

    @Bindable
    public String getCaption() {
        if (getItem() == null) {
            return "";
        }
        return getItem().getCaption();
    }

    @Bindable
    public int getLikes() {
        if (getItem() == null) {
            return 0;
        }
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
        if (getItem() == null) {
            return false;
        }
        return getItem().isLiked();
    }

    @Bindable
    public boolean isReposted() {
        if (getItem() == null) {
            return false;
        }
        return getItem().isReposted();
    }

    @Bindable
    public String getCommentsCount() {
        if (activity.get() == null) {
            return "";
        }
        if (getItem() == null || getItem().getCommentCount() == 0) {
            return activity.get().getString(R.string.read_more);
        }
        return activity.get().getResources().getQuantityString(R.plurals.comments, getItem().getCommentCount(), getItem().getCommentCount());
    }

    @Bindable
    public String getRepostedBy() {
        if (!showReposted || getItem() == null || getItem().getRepostedBy() == null) {
            return "";
        }
        return getItem().getRepostedBy().toUpperCase();
    }

    public Action1<View> readMore = view -> {
        String className = activity.get().getLocalClassName();

        if (activity.get() instanceof HomeActivity) {
//            analyticsManager.galleryOpenedFromHighlights(getId());
            analyticsManager.galleryOpened(getId(), "highlights", null);
        }
        else if (activity.get() instanceof StoryGalleryActivity) {
//            analyticsManager.galleriesOpenedFromStories(getId());
            if (getId() != null) {
                analyticsManager.galleryOpened(getId(), "stories", null);
            }
        }
        else if (activity.get() instanceof ProfileActivity) {
            ProfileActivity profileActivity = (ProfileActivity) activity.get();
//            analyticsManager.galleriesOpenedFromProfile(getId(), profileActivity.getUserId());
            analyticsManager.galleryOpened(getId(), "profile", profileActivity.getUserId());
        }
        else if (activity.get() instanceof GalleryListActivity) {
            analyticsManager.galleryOpened(getId(), "push", null);
        }
        else if (activity.get() instanceof SearchActivity) {
            analyticsManager.galleryOpened(getId(), "search", null);
        }
        else {
            LogUtils.i(TAG, "where lol - " + className);
        }

        ViewCompat.setTransitionName(viewPager.get(), activity.get().getString(R.string.post_transition));
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity.get(), viewPager.get(), activity.get().getString(R.string.post_transition));

        // Transition Animation to gallery detail
        GalleryDetailActivity.start(view.getContext(), getId(), viewPager.get().getCurrentItem(), options.toBundle());
    };

    public Action1<View> share = view -> {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, EndpointHelper.currentEndpoint.frescoWebsite + "gallery/" + getId());

        //For Mixpanel
        String className = activity.get().getLocalClassName();
        String openedFrom = className;
        String id = "null";
        String userId = null;
        if (getId() != null) {
            id = getId();
        }
        if (activity.get() instanceof HomeActivity) {
//            analyticsManager.gallerySharedFromHighlights(getId());
            openedFrom = "highlights";
        }
        else if (activity.get() instanceof StoryGalleryActivity) {
//            analyticsManager.gallerySharedFromStories(getId());
            openedFrom = "stories";
            LogUtils.i(TAG, "classname - " + className);
        }
        else if (activity.get() instanceof ProfileActivity) {
            ProfileActivity profileActivity = (ProfileActivity) activity.get();
//            analyticsManager.gallerySharedFromProfile(getId(), profileActivity.getUserId());
            userId = profileActivity.getUserId();
            openedFrom = "profile";
        }
        else if (activity.get() instanceof GalleryListActivity) {
            openedFrom = "push";
        }
        else if (activity.get() instanceof SearchActivity) {
            openedFrom = "search";
        }
        analyticsManager.galleryShared(id, openedFrom, userId);

        activity.get().startActivity(Intent.createChooser(intent, "Share Gallery"));
    };

    public Action1<View> goToProfile = view -> {
        userManager.downloadUser(getRepostedBy())
                   .onErrorReturn(throwable -> null)
                   .subscribe(user -> {
                       if (user != null) {
                           ProfileActivity.start(view.getContext(), user.getId(), false, true, false);
                       }
                   });
    };

    public Action1<View> repost = view -> {
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(activity.get(), OnboardingActivity.class);
            activity.get().startActivity(intent);
            return;
        }

        //Can't repost your own gallery
        if (sessionManager.getCurrentSession().getUserId().equals(getItem().getOwnerId())) {
            return;
        }

        //For Mixpanel
        String className = activity.get().getLocalClassName();
        String openedFrom = className;
        String id = "null";
        String userId = null;
        if (getId() != null) {
            id = getId();
        }
        LogUtils.i(TAG, "classname - " + className);
        if (activity.get() instanceof HomeActivity) {
            openedFrom = "highlights";
        }
        else if (activity.get() instanceof StoryGalleryActivity) {
            openedFrom = "stories";
        }
        else if (activity.get() instanceof ProfileActivity) {
            ProfileActivity profileActivity = (ProfileActivity) activity.get();
            userId = profileActivity.getUserId();
            openedFrom = "profile";
        }
        else if (activity.get() instanceof GalleryListActivity) {
            openedFrom = "push";
        }
        else if (activity.get() instanceof SearchActivity) {
            openedFrom = "search";
        }

        if (isReposted()) {
            galleryManager.unrepost(getItem())
                          .onErrorReturn(throwable -> null)
                          .subscribe(success -> updateReposts());
            analyticsManager.galleryUnreposted(id, openedFrom, userId);
        }
        else {
            galleryManager.repost(getItem())
                          .onErrorReturn(throwable -> null)
                          .subscribe(success -> updateReposts());
            analyticsManager.galleryReposted(id, openedFrom, userId);
        }
        updateReposts();
    };

    public Action1<View> like = view -> {
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(activity.get(), OnboardingActivity.class);
            activity.get().startActivity(intent);
            return;
        }

        //For Mixpanel
        String className = activity.get().getLocalClassName();
        String openedFrom = className;
        String id = "null";
        String userId = null;
        if (getId() != null) {
            id = getId();
        }
        LogUtils.i(TAG, "classname - " + className);
        if (activity.get() instanceof HomeActivity) {
            openedFrom = "highlights";
        }
        else if (activity.get() instanceof StoryGalleryActivity) {
            openedFrom = "stories";
        }
        else if (activity.get() instanceof ProfileActivity) {
            ProfileActivity profileActivity = (ProfileActivity) activity.get();
            userId = profileActivity.getUserId();
            openedFrom = "profile";
        }
        else if (activity.get() instanceof GalleryListActivity) {
            openedFrom = "push";
        }
        else if (activity.get() instanceof SearchActivity) {
            openedFrom = "search";
        }

        if (isLiked()) {
            galleryManager.unlike(getItem())
                          .onErrorReturn(throwable -> null)
                          .subscribe(success -> updateLikes());
            analyticsManager.galleryDisliked(id, openedFrom, userId);
        }
        else {
            galleryManager.like(getItem())
                          .onErrorReturn(throwable -> null)
                          .subscribe(success -> updateLikes());
            analyticsManager.galleryLiked(id, openedFrom, userId);
        }
        updateLikes();
    };

    private void updateReposts() {
        notifyPropertyChanged(BR.reposts);
        notifyPropertyChanged(BR.reposted);
    }

    private void updateLikes() {
        notifyPropertyChanged(BR.likes);
        notifyPropertyChanged(BR.liked);
    }

    public Action1<View> showLikes = view -> {
        FollowActivity.start(activity.get(), getId(), GALLERY_LIKES_STATE);
    };
    public Action1<View> showReposts = view -> {
        FollowActivity.start(activity.get(), getId(), GALLERY_REPOSTS_STATE);
    };

}

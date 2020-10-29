package com.fresconews.fresco.framework.mvvm.viewmodels;

import android.app.Activity;
import android.content.Intent;
import android.databinding.Bindable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.framework.mvvm.ItemViewModel;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.Session;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.v2.follow.following.FollowingFragment;
import com.fresconews.fresco.v2.follow.following.FollowingViewModel;
import com.fresconews.fresco.v2.onboarding.OnboardingActivity;
import com.fresconews.fresco.v2.profile.ProfileActivity;
import com.fresconews.fresco.v2.utils.StringUtils;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Blaze on 8/4/2016.
 */
public class UserSearchViewModel extends ItemViewModel<User> {
    public static final String TAG = UserSearchViewModel.class.getSimpleName();

    private WeakReference<Activity> mActivity;
    private WeakReference<Fragment> fragment;

    @Inject
    UserManager userManager;

    @Inject
    SessionManager sessionManager;

    public UserSearchViewModel(Activity paramActivity, User paramItem) {
        super(paramItem);
        ((Fresco2) paramActivity.getApplication()).getFrescoComponent().inject(this);
        mActivity = new WeakReference<>(paramActivity);
    }

    public UserSearchViewModel(Fragment paramFrag, User paramItem) {
        super(paramItem);
        ((Fresco2) paramFrag.getActivity().getApplication()).getFrescoComponent().inject(this);
        fragment = new WeakReference<>(paramFrag);
    }

    public String getId() {
        return getItem().getId();
    }

    @Bindable
    public String getUsername() {
        if (StringUtils.toNullIfEmpty(getItem().getUsername()) == null || getItem().getUsername().equals("null")) {
            return "";
        }
        return "@" + getItem().getUsername();
    }

    @Bindable
    public String getFullName() {
        if (getItem().getFullName() == null) {
            return "";
        }
        return getItem().getFullName();
    }

    @Bindable
    public String getBio() {
        if (getItem().getBio() == null) {
            return "";
        }
        return getItem().getBio().trim();
    }

    @Bindable
    public String getAvatarUrl() {
        return getItem().getAvatar();
    }

    @Bindable
    public boolean isFollowing() {
        return getItem().isFollowing();
    }

    @Bindable
    public boolean isMe() {
        Session session = sessionManager.getCurrentSession();
        return session != null && session.getUserId() != null && session.getUserId().equals(getId());
    }

    public Action1<View> toggleFollow = view -> {
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            if (mActivity != null && mActivity.get() != null) {
                Intent intent = new Intent(mActivity.get(), OnboardingActivity.class);
                mActivity.get().startActivity(intent);
                return;
            }
            if (fragment != null && fragment.get() != null) {
                Intent intent = new Intent(fragment.get().getActivity(), OnboardingActivity.class);
                fragment.get().getActivity().startActivity(intent);
                return;
            }
            return;
        }

        if (isFollowing()) {
            //noinspection unchecked
            userManager.unfollow(getItem())
                       .observeOn(AndroidSchedulers.mainThread())
                       .onErrorReturn(throwable -> null)
                       .subscribe(success -> {
                           updateFollowing();
                           if (fragment == null || fragment.get() == null) {
                               return;
                           }
                           if (fragment.get() instanceof FollowingFragment && isMe()) {
                               FollowingFragment x = (FollowingFragment) fragment.get();
                               FollowingViewModel followingViewModel = (FollowingViewModel) x.getViewModel();
                               followingViewModel.removeUserFromFollowing(getItem());
                           }
                       });
        }
        else {
            userManager.follow(getItem())
                       .onErrorReturn(throwable -> null)
                       .subscribe(success -> updateFollowing());
        }
        updateFollowing();

    };

    private void updateFollowing() {
        notifyPropertyChanged(BR.following);
    }

    public Action1<View> profilePictureClicked = view -> {
        ProfileActivity.start(view.getContext(), getItem().getId(), true, true, false);
    };
}

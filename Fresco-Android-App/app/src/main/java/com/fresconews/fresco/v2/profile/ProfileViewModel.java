package com.fresconews.fresco.v2.profile;

import android.content.Intent;
import android.databinding.Bindable;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.persistence.managers.AuthManager;
import com.fresconews.fresco.framework.persistence.managers.FeedManager;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.framework.persistence.managers.NotificationFeedManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.Session;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.v2.editprofile.EditProfileActivity;
import com.fresconews.fresco.v2.follow.FollowActivity;
import com.fresconews.fresco.v2.notificationfeed.NotificationFeedActivity;
import com.fresconews.fresco.v2.notifications.INotificationActivityViewModel;
import com.fresconews.fresco.v2.report.ReportBottomSheetDialogFragment;
import com.fresconews.fresco.v2.report.ReportBottomSheetDialogViewModel;
import com.fresconews.fresco.v2.report.ReportUserDialogViewModel;
import com.fresconews.fresco.v2.utils.DialogUtils;
import com.fresconews.fresco.v2.utils.NetworkUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.fresconews.fresco.v2.utils.StringUtils;
import com.google.gson.Gson;
import com.viewpagerindicator.CirclePageIndicator;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.fresconews.fresco.v2.profile.ProfileActivity.EDIT_REQUEST_CODE;

/**
 * Created by Blaze on 7/13/2016.
 */
public class ProfileViewModel extends ActivityViewModel<ProfileActivity> implements INotificationActivityViewModel {
    private static final String TAG = ProfileViewModel.class.getSimpleName();

    @Inject
    GalleryManager galleryManager;

    @Inject
    AuthManager authManager;

    @Inject
    SessionManager sessionManager;

    @Inject
    FeedManager feedManager;

    @Inject
    NotificationFeedManager notificationFeedManager;

    @Inject
    UserManager userManager;

    public BindableView<AppBarLayout> appBarLayout = new BindableView<>();
    public BindableView<ViewPager> viewPager = new BindableView<>();
    public BindableView<TabLayout> tabLayout = new BindableView<>();
    public BindableView<View> userInfoLayout = new BindableView<>();

    public BindableView<CirclePageIndicator> circleIndicator = new BindableView<>();
    public BindableView<CollapsingToolbarLayout> collapsingToolbar = new BindableView<>();

    private ReportBottomSheetDialogFragment reportFragment;

    private User user;
    private String userId;
    private String name;
    private String avatarUrl;
    private String selfAvatarUrl;
    private String bio;
    private String username;
    private String location;
    private String followedCount;
    private Boolean isOwner;
    private boolean follow;
    private boolean following;
    private boolean locationFilled;

    private boolean suspended;
    private boolean blocked;
    private boolean blocking;
    private String suspendedString;

    private boolean disabled;

    private int unreadNotifications;

    public ProfileViewModel(ProfileActivity activity, String userId, boolean follow) {
        super(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        setNavIcon(R.drawable.ic_navigation_arrow_back_white);

        // Initialize
        this.userId = userId;
        setIsOwner(false);

        // Confirm if owner of profile, check if user even logged in
        if (sessionManager.isLoggedIn() && userId.equals(sessionManager.getCurrentSession().getUserId())) {
            setIsOwner(true);
        }

        this.follow = follow;

        setBio(getString(R.string.bio_loading));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isLoggedIn()) {
            notificationSubscription = notificationFeedManager.pollUnseenNotificationsCount()
                                                              .observeOn(AndroidSchedulers.mainThread())
                                                              .onErrorReturn(throwable -> null)
                                                              .subscribe(integer -> {
                                                                  if(integer == null){
                                                                      return;
                                                                  }
                                                                  setUnreadNotifications(integer);
                                                                  notifyPropertyChanged(BR.unreadNotifications);
                                                              });
        }
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }
        super.onBound();

        collapsingToolbar.get().setExpandedTitleColor(Color.argb(0x00, 0xff, 0xff, 0xff));

        ProfilePagerAdapter adapter = new ProfilePagerAdapter(getActivity().getSupportFragmentManager(), userId);
        if (viewPager != null) {
            viewPager.get().setAdapter(adapter);
            tabLayout.get().setupWithViewPager(viewPager.get());
        }

        downloadUser();
    }

    @Override
    public void refreshHeader(Session session) {
        super.refreshHeader(session);
        refreshNotifications();
    }

    public void downloadUser() {
        // Update bound fields
        userManager.downloadUser(userId)
                   .observeOn(AndroidSchedulers.mainThread())
                   .onErrorReturn(throwable -> null)
                   .subscribe(user -> {
                       if (user == null) {
                           setAvatarUrl(null);
                           setFollowedCount("");
                           setLocation("");
                           setName("");
                           setUsername("");
                           setFollowing(false);
                           setBio("");
                           setLocationFilled(false);
                       }
                       else {
                           this.user = user;
                           setDisabled(user.isDisabled());
                           setSuspended(!TextUtils.isEmpty(user.getSuspendedUntil()));
                           setBlocked(user.isBlocked());
                           setBlocking(user.isBlocking());
                           setAvatarUrl(user.getAvatar());
                           setFollowedCount(StringUtils.abbrNum(user.getFollowedCount(), 1));
                           setLocation(user.getLocation());
                           setName(user.getFullName());
                           setUsername(user.getUsername());
                           setFollowing(user.isFollowing());
                           setBio(user.getBio());
                           setLocationFilled(!TextUtils.isEmpty(user.getLocation()));
                       }

                       if (follow) {
                           followUser();
                           setFollowing(true);
                       }

                       FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) userInfoLayout.get().getLayoutParams();
                       params.gravity = isUserInfoEmpty(user) ? Gravity.CENTER : Gravity.START;
                       userInfoLayout.get().setLayoutParams(params);

                       if (isLoggedIn()) {
                           userManager.me()
                                      .observeOn(AndroidSchedulers.mainThread())
                                      .onErrorReturn(throwable -> null)
                                      .subscribe(user1 -> {
                                          if (user1 != null) {
                                              setSelfAvatarUrl(user1.getAvatar());
                                          }
                                      });
                       }
                   });
    }

    private void refreshNotifications() {
        if (sessionManager.isLoggedIn()) {
            notificationFeedManager.getUnseenNotificationsCount()
                                   .observeOn(AndroidSchedulers.mainThread())
                                   .onErrorReturn(throwable -> null)
                                   .subscribe(integer -> {
                                       if (integer == null) {
                                           setUnreadNotifications(0);
                                       }
                                       else {
                                           setUnreadNotifications(integer);
                                       }
                                       notifyPropertyChanged(BR.unreadNotifications);
                                   });
        }
    }

    private boolean isUserInfoEmpty(User user) {
        return user == null || (!isBioFilled(user) && !isLocationFilled() && TextUtils.isEmpty(user.getFullName()));
    }

    private boolean isBioFilled(User user) {
        return user != null && !TextUtils.isEmpty(user.getBio());
    }

    @Bindable
    public boolean isLocationFilled() {
        return locationFilled;
    }

    public void setLocationFilled(boolean locationFilled) {
        this.locationFilled = locationFilled;
        notifyPropertyChanged(BR.locationFilled);
    }

    @Bindable
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        if (TextUtils.isEmpty(bio)) {
            this.bio = "";
        }
        else {
            this.bio = bio.trim();
        }
        notifyPropertyChanged(BR.bio);
    }

    @Bindable
    public String getFollowedCount() {
        return followedCount;
    }

    public void setFollowedCount(String followedCount) {
        if (TextUtils.isEmpty(followedCount)) {
            this.followedCount = "";
        }
        else {
            this.followedCount = followedCount;
        }
        notifyPropertyChanged(BR.followedCount);
    }

    @Bindable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String url) {
        avatarUrl = url;
        notifyPropertyChanged(BR.avatarUrl);
    }

    @Bindable
    public String getSelfAvatarUrl() {
        if (TextUtils.isEmpty(selfAvatarUrl)) {
            return "res:///" + R.drawable.account;
        }
        return selfAvatarUrl;
    }

    public void setSelfAvatarUrl(String url) {
        selfAvatarUrl = url;
        notifyPropertyChanged(BR.selfAvatarUrl);
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (TextUtils.isEmpty(name)) {
            this.name = "";
        }
        else {
            this.name = name;
        }
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if (TextUtils.isEmpty(location)) {
            this.location = "";
        }
        else {
            this.location = location;
        }
        notifyPropertyChanged(BR.location);
    }

    @Bindable
    public String getUsername() {
        if (TextUtils.isEmpty(username)) {
            return "";
        }
        return "@" + username;
    }

    public void setUsername(String username) {
        this.username = username;
        notifyPropertyChanged(BR.username);
    }

    @Bindable
    public Boolean getIsOwner() {
        return (isOwner);
    }

    public void setIsOwner(Boolean isOwner) {
        this.isOwner = isOwner;
        notifyPropertyChanged(BR.isOwner);
    }

    @Bindable
    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
        notifyPropertyChanged(BR.following);
    }

    @Bindable
    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
        notifyPropertyChanged(BR.suspended);
        if (suspended) {
            CoordinatorLayout.LayoutParams appBarLayoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.get().getLayoutParams();
            AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) appBarLayoutParams.getBehavior();
            if (behavior != null) {
                behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                    @Override
                    public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                        return false;
                    }
                });
            }
        }
    }

    @Bindable
    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
        notifyPropertyChanged(BR.blocking);
    }

    @Bindable
    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
        notifyPropertyChanged(BR.blocked);
    }

    @Bindable
    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        notifyPropertyChanged(BR.disabled);
    }

    @Bindable
    public boolean isSignedIn() {
        return sessionManager.isLoggedIn();
    }

    public Action1<View> edit = view -> {
        EditProfileActivity.start(getActivity(), userId, EDIT_REQUEST_CODE);
    };

    public Action1<View> showFollowers = view -> {
        FollowActivity.start(getActivity(), userId);
    };

    public Action1<View> showOptions = view -> {
        if (view == null) {
            return;
        }
        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_check_internet);
            return;
        }

        reportFragment = ReportBottomSheetDialogFragment.newInstance(null, user, new ReportBottomSheetDialogViewModel.OnReportAndBlockListener() {
            @Override
            public void onReportGallery() {
                reportFragment.dismiss();
            }

            @Override
            public void onToggleFollow(boolean following) {
                if (following) {
                    unFollowUser();
                    notifyPropertyChanged(BR.followedCount);
                }
                else {
                    followUser();
                    notifyPropertyChanged(BR.followedCount);
                }
                setFollowing(isFollowing());
                reportFragment.dismiss();
            }

            @Override
            public void onReportUser() {
                ReportUserDialogViewModel reportUserDialogViewModel = new ReportUserDialogViewModel(getActivity(), (reason, message) -> {
                    userManager.report(userId, reason, message)
                               .subscribeOn(Schedulers.io())
                               .observeOn(AndroidSchedulers.mainThread())
                               .onErrorReturn(throwable -> null)
                               .subscribe(networkReport -> {
                                   if (networkReport == null) {
                                       SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_reporting_user);
                                       return;
                                   }
                                   Log.e(TAG, new Gson().toJson(networkReport));
                                   if (isBlocking()) {
                                       DialogUtils.showFrescoDialog(getActivity(), R.string.report_sent, R.string.report_sent_message, R.string.youre_welcome,
                                               (dialogInterface, i) -> dialogInterface.dismiss());
                                   }
                                   else {
                                       DialogUtils.showFrescoDialog(getActivity(), R.string.report_sent, getActivity().getString(R.string.report_sent_user_message, user.getUsername()),
                                               R.string.block_user_static, ((dialogInterface, i) -> {
                                                   onToggleBlockUser(true);
                                                   dialogInterface.dismiss();
                                               }),
                                               R.string.close, (dialogInterface, i) -> dialogInterface.dismiss());
                                   }
                               });
                    reportFragment.dismiss();
                });
                reportUserDialogViewModel.show(R.layout.view_report_user);
                reportUserDialogViewModel.setReportTitle(getString(R.string.report_user, user.getUsername()));
            }

            @Override
            public void onToggleBlockUser(boolean blocking) {
                if (blocking) {
                    userManager.block(userId)
                               .subscribeOn(Schedulers.io())
                               .observeOn(AndroidSchedulers.mainThread())
                               .onErrorReturn(throwable -> null)
                               .subscribe(networkSuccessResult -> {
                                   if (networkSuccessResult != null && !TextUtils.isEmpty(networkSuccessResult.getSuccess())) {
                                       setBlocking(true);
                                       user.setBlocking(true);
                                       user.save();
                                       reportFragment.refreshBlockText(getActivity(), user);
                                       //Show dialog letting user know consequencecs of blocking.
                                       if (isBlocking()) {
                                           DialogUtils.showFrescoDialog(getActivity(), R.string.blocked_text, getActivity().getString(R.string.blocked_dialog_message, user.getUsername()),
                                                   R.string.ok, ((dialogInterface, i) -> {
                                                       dialogInterface.dismiss();
                                                   }),
                                                   R.string.undo, (dialogInterface, i) -> {
                                                       onToggleBlockUser(false); //unblock user
                                                       dialogInterface.dismiss();
                                                   });
                                       }
                                   }
                                   else {
                                       SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_blocking_user);
                                   }
                               });
                }
                else {
                    userManager.unblock(userId)
                               .onErrorReturn(throwable -> null)
                               .subscribe(networkSuccessResult -> {
                                   if (networkSuccessResult != null && !TextUtils.isEmpty(networkSuccessResult.getSuccess())) {
                                       setBlocking(false);
                                       user.setBlocking(false);
                                       user.save();
                                       reportFragment.refreshBlockText(getActivity(), user);
                                   }
                                   else {
                                       SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_unblocking_user);
                                   }
                               });
                }
                reportFragment.dismiss();
            }

            @Override
            public void onDeleteComment() {
                reportFragment.dismiss();
            }
        });
        reportFragment.show(getActivity().getSupportFragmentManager(), TAG);
    };

    private void unFollowUser() {
        if (user != null) {
            userManager.unfollow(user)
                       .subscribeOn(Schedulers.io())
                       .observeOn(AndroidSchedulers.mainThread())
                       .onErrorReturn(throwable -> null)
                       .filter(success -> success != null)
                       .subscribe(success -> {
                           user.setFollowing(false);
                           user.save();
                           setFollowing(false);
                       });
        }
    }

    private void followUser() {
        if (user != null) {
            userManager.follow(user)
                       .subscribeOn(Schedulers.io())
                       .observeOn(AndroidSchedulers.mainThread())
                       .onErrorReturn(throwable -> null)
                       .filter(success -> success != null)
                       .subscribe(success -> {
                           user.setFollowing(true);
                           user.save();
                           setFollowing(true);
                       });
        }
    }

    @Bindable
    public int getUnreadNotifications() {
        return unreadNotifications;
    }

    public void setUnreadNotifications(int unreadNotifications) {
        this.unreadNotifications = unreadNotifications;
    }

    @Bindable
    public boolean isLoggedIn() {
        return super.isLoggedIn();
    }

    public Action1<View> profilePictureClicked = view -> {
        NotificationFeedActivity.start(getActivity());
    };

    @Override
    public void onNotificationReceived() {
        refreshNotifications();
    }

    @Bindable
    public String getSuspendedString() {
        return getString(R.string.suspended);
    }

    @Bindable
    public String getBlockedString() {
        return getString(R.string.blocked);
    }
}

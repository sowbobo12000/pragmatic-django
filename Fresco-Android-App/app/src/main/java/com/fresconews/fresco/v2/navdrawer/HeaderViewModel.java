package com.fresconews.fresco.v2.navdrawer;

import android.app.Activity;
import android.content.Intent;
import android.databinding.Bindable;
import android.text.TextUtils;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.framework.mvvm.ViewModel;
import com.fresconews.fresco.framework.persistence.managers.AuthManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.Session;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.v2.home.HomeActivity;
import com.fresconews.fresco.v2.onboarding.OnboardingActivity;
import com.fresconews.fresco.v2.profile.ProfileActivity;
import com.fresconews.fresco.v2.utils.DimensionUtils;
import com.fresconews.fresco.v2.utils.ImageUtils;
import com.fresconews.fresco.v2.utils.LogUtils;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;

public class HeaderViewModel extends ViewModel {
    private static final String TAG = HeaderViewModel.class.getSimpleName();

    private boolean loggedIn;
    private String username;
    private String avatar;

    @Inject
    SessionManager sessionManager;

    @Inject
    AuthManager authManager;

    @Inject
    UserManager userManager;

    private Activity activity;

    public HeaderViewModel(Activity activity) {
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);
        setSession(sessionManager.getCurrentSession());
        this.activity = activity;
    }

    public void setSession(Session session) {
        if (session == null || session.getUsername() == null || sessionManager == null) {
            setLoggedIn(false);
            return;
        }
//        setLoggedIn(!TextUtils.isEmpty(session.getUsername()) && !TextUtils.isEmpty(session.getUserId()));
        setLoggedIn(sessionManager.isLoggedIn());
        setUsername(session.getUsername());

        userManager.getOrDownloadUser(session.getUserId()).onErrorReturn(throwable -> null).subscribe(user -> {
            if (user != null && activity != null) {
                setAvatar(ImageUtils.getImageSizeV2(user.getAvatar(), DimensionUtils.convertDpToPixel(25, activity)));
            }
        });
    }

    public Action1<View> onLoginClicked = view -> {
        if (activity instanceof HomeActivity) {
            LogUtils.i(TAG, "Instance of home activity");
            OnboardingActivity.start(activity, "HOME");
        }
        else {
            Intent intent = new Intent(activity, OnboardingActivity.class);
            activity.startActivity(intent);
        }
    };

    public Action1<View> onProfileClicked = view -> {
        if (sessionManager.isLoggedIn()) {
            ProfileActivity.start(activity, sessionManager.getCurrentSession().getUserId());
        }
    };

    @Bindable
    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
        notifyPropertyChanged(BR.loggedIn);
    }

    @Bindable
    public String getUsername() {
        getUser().onErrorReturn(throwable -> null).subscribe(user -> {
            if (user != null && username != null && !TextUtils.isEmpty(user.getUsername())) {
                if (!user.getUsername().equals(username)) {
                    setUsername(user.getUsername());
                }
            }
        });
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        notifyPropertyChanged(BR.username);
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
        notifyPropertyChanged(BR.avatar);
    }

    private Observable<User> getUser() {
        return userManager.me();
    }
}

package com.fresconews.fresco.framework.mvvm.viewmodels;

import android.app.Activity;
import android.databinding.Bindable;
import android.view.View;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.framework.mvvm.ItemViewModel;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.Session;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.v2.utils.StringUtils;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import rx.functions.Action1;

/**
 * Created by wumau on 8/4/2016.
 */
public class UserAutoCompleteViewModel extends ItemViewModel<User> {
    public static final String TAG = UserAutoCompleteViewModel.class.getSimpleName();

    private WeakReference<Activity> activity;
    private OnUserSelectListener onUserSelectListener;

    @Inject
    UserManager userManager;

    @Inject
    SessionManager sessionManager;

    public UserAutoCompleteViewModel(Activity paramActivity, User paramItem, OnUserSelectListener onUserSelectListener) {
        super(paramItem);
        ((Fresco2) paramActivity.getApplication()).getFrescoComponent().inject(this);
        activity = new WeakReference<>(paramActivity);
        this.onUserSelectListener = onUserSelectListener;
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
    public String getAvatarUrl() {
        return getItem().getAvatar();
    }

    @Bindable
    public boolean isMe() {
        Session session = sessionManager.getCurrentSession();
        return session != null && session.getUserId().equals(getId());
    }

    public Action1<View> profilePictureClicked = view -> {
        onUserSelectListener.onUserSelect(getUsername());
    };

    public interface OnUserSelectListener {
        void onUserSelect(String username);
    }
}

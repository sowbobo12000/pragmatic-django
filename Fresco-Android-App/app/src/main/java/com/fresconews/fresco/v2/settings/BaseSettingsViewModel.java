package com.fresconews.fresco.v2.settings;

import android.view.View;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.mvvm.ViewModel;
import com.fresconews.fresco.framework.persistence.managers.AuthManager;
import com.fresconews.fresco.framework.persistence.managers.PaymentManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.User;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by mauricewu on 11/3/16.
 */
public class BaseSettingsViewModel extends ViewModel {

    @Inject
    UserManager userManager;

    @Inject
    SessionManager sessionManager;

    @Inject
    PaymentManager paymentManager;

    @Inject
    AuthManager authManager;

    protected ActivityViewModel activityViewModel;

    public BaseSettingsViewModel(ActivityViewModel activityViewModel) {
        this.activityViewModel = activityViewModel;

        ((Fresco2) activityViewModel.getActivity().getApplication()).getFrescoComponent().inject(this);
    }

    protected Observable<User> getUser() {
        return userManager.me();
    }

    protected BaseActivity getActivity() {
        return activityViewModel.getActivity();
    }

    protected View getRoot() {
        if (getActivity().getDataBinding().getRoot() == null) {
            return null;
        }
        return getActivity().getDataBinding().getRoot();
    }
}

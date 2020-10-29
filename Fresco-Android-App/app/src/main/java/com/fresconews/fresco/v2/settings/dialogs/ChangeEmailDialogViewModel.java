package com.fresconews.fresco.v2.settings.dialogs;

import android.app.Activity;
import android.databinding.Bindable;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableString;
import com.fresconews.fresco.framework.mvvm.DialogViewModel;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.v2.utils.NetworkUtils;
import com.fresconews.fresco.v2.utils.StringUtils;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by mauricewu on 10/28/16.
 */

public class ChangeEmailDialogViewModel extends DialogViewModel {

    @Inject
    UserManager userManager;

    public BindableString newEmail = new BindableString();
    public BindableString newPassword = new BindableString();

    private String emailError;
    private String passwordError;
    private OnChangeEmailListener listener;

    public ChangeEmailDialogViewModel(Activity activity, String email, OnChangeEmailListener listener) {
        super(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);
        this.listener = listener;
        newEmail.set(email);
    }

    @Override
    public void onBound() {
        super.onBound();
    }

    public void reload(String email) {
        newEmail.set(email);
        newPassword.set("");
        setEmailError("");
        setPasswordError("");
    }

    private boolean isValid() {
        boolean valid = true;
        if (newEmail.get().isEmpty()) {
            setEmailError(activity.getString(R.string.error_email_empty));
            valid = false;
        }
        else if (!StringUtils.isEmailValid(newEmail.get())) {
            setEmailError(activity.getString(R.string.error_email_invalid));
            valid = false;
        }
        if (newPassword.get().isEmpty()) {
            setPasswordError(activity.getString(R.string.error_password_empty));
            valid = false;
        }
        return valid;
    }

    public Action1<View> cancel = view -> dismiss();

    public Action1<View> save = view -> {
        if (isValid()) {
            //Check if email exists
            userManager.checkEmail(newEmail.get())
                       .observeOn(AndroidSchedulers.mainThread())
                       .onErrorReturn(throwable -> null)
                       .subscribe(networkUserCheck -> {
                           boolean available = true;
                           if (networkUserCheck == null) {
                               available = false;
                           }
                           else if (!networkUserCheck.isAvailable()) {
                               available = false;
                               String[] fieldsUnavailable = networkUserCheck.getFieldsUnavailable();
                               for (int i = 0; i < fieldsUnavailable.length; i++) {
                                   String field = fieldsUnavailable[i];
                                   if (field.equals("email")) {
                                       available = false;
                                   }
                               }
                           }
                           if (available) {
                               if (listener != null) {
                                   listener.onSaveClick(newEmail.get(), newPassword.get());
                               }
                               dismiss();
                           }
                           else {
                               //Check for internet connection
                               if (!NetworkUtils.isNetworkAvailable(activity)) {
                                   setEmailError(activity.getString(R.string.error_check_internet));
                               }
                               else {
                                   setEmailError(activity.getString(R.string.error_email_exists));
                               }
                           }
                       });
        }
    };

    @Bindable
    public String getEmailError() {
        return emailError;
    }

    public void setEmailError(String emailError) {
        this.emailError = emailError;
        notifyPropertyChanged(BR.emailError);
    }

    @Bindable
    public String getPasswordError() {
        return passwordError;
    }

    public void setPasswordError(String passwordError) {
        this.passwordError = passwordError;
        notifyPropertyChanged(BR.passwordError);
    }

    public interface OnChangeEmailListener {
        void onSaveClick(String username, String password);
    }
}

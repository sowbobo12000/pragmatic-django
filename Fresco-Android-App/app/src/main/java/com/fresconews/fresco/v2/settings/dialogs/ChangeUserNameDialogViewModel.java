package com.fresconews.fresco.v2.settings.dialogs;

import android.app.Activity;
import android.databinding.Bindable;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableString;
import com.fresconews.fresco.framework.mvvm.DialogViewModel;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.NetworkUtils;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by mauricewu on 10/28/16.
 */

public class ChangeUserNameDialogViewModel extends DialogViewModel {

    @Inject
    UserManager userManager;

    public BindableString newUsername = new BindableString();
    public BindableString newPassword = new BindableString();

    private String usernameError;
    private String passwordError;
    private OnChangeUserNameListener listener;

    public ChangeUserNameDialogViewModel(Activity activity, String userName, OnChangeUserNameListener listener) {
        super(activity);

        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);
        this.listener = listener;

        newUsername.set(userName);
    }

    public void reload(String userName) {
        newUsername.set(userName);
        newPassword.set("");
        setUsernameError("");
        setPasswordError("");
    }

    @Override
    public void onBound() {
        super.onBound();
    }

    private boolean isValid() {
        boolean valid = true;

        if (newUsername.get().isEmpty()) {
            LogUtils.i("made it", "this far 2");
            setUsernameError(activity.getString(R.string.error_username_empty));
            valid = false;
        }
        if (newPassword.get().isEmpty()) {
            LogUtils.i("made it", "this far 3");
            setPasswordError(activity.getString(R.string.error_password_empty));
            valid = false;
        }

        return valid;
    }

    public Action1<View> cancel = view -> dismiss();

    public Action1<View> save = view -> {
        if (isValid()) {
            //Check if username exists
            userManager.checkUsername(newUsername.get())
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
                                   if (field.equals("username")) {
                                       available = false;
                                   }
                               }
                           }
                           if (available) {
                               if (listener != null) {
                                   listener.onSaveClick(newUsername.get(), newPassword.get());
                               }
                               dismiss();
                           }
                           else {
                               //Check for internet connection
                               if (!NetworkUtils.isNetworkAvailable(activity)) {
                                   setUsernameError(activity.getString(R.string.error_check_internet));
                               }
                               else {
                                   setUsernameError(activity.getString(R.string.error_username_exists));
                               }
                           }
                       });
        }
    };

    @Bindable
    public String getUsernameError() {
        return usernameError;
    }

    public void setUsernameError(String usernameError) {
        this.usernameError = usernameError;
        notifyPropertyChanged(BR.usernameError);
    }

    @Bindable
    public String getPasswordError() {
        return passwordError;
    }

    public void setPasswordError(String passwordError) {
        this.passwordError = passwordError;
        notifyPropertyChanged(BR.passwordError);
    }

    public interface OnChangeUserNameListener {
        void onSaveClick(String username, String password);
    }
}

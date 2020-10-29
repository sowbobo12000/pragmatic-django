package com.fresconews.fresco.v2.settings.dialogs;

import android.app.Activity;
import android.databinding.Bindable;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableString;
import com.fresconews.fresco.framework.mvvm.DialogViewModel;

import rx.functions.Action1;

/**
 * Created by mauricewu on 10/28/16.
 */

public class ChangePasswordDialogViewModel extends DialogViewModel {

    public BindableString oldPassword = new BindableString();
    public BindableString newPassword = new BindableString();

    private String newPasswordError;
    private String oldPasswordError;
    private OnChangePasswordListener listener;

    public ChangePasswordDialogViewModel(Activity activity, OnChangePasswordListener listener) {
        super(activity);
        this.listener = listener;
    }

    public void reload() {
        oldPassword.set("");
        newPassword.set("");
        setOldPasswordError("");
        setNewPasswordError("");
    }

    @Override
    public void onBound() {
        super.onBound();
    }

    private boolean isValid() {
        boolean valid = true;
        if (oldPassword.get().isEmpty()) {
            setNewPasswordError(activity.getString(R.string.error_password_empty));
            valid = false;
        }
        if (newPassword.get().isEmpty()) {
            setOldPasswordError(activity.getString(R.string.error_password_empty));
            valid = false;
        }

        return valid;
    }

    public Action1<View> cancel = view -> dismiss();

    public Action1<View> save = view -> {
        if (isValid()) {
            if (listener != null) {
                listener.onSaveClick(oldPassword.get(), newPassword.get());
            }
            dismiss();
        }
    };

    @Bindable
    public String getNewPasswordError() {
        return newPasswordError;
    }

    public void setNewPasswordError(String newPasswordError) {
        this.newPasswordError = newPasswordError;
        notifyPropertyChanged(BR.emailError);
    }

    @Bindable
    public String getOldPasswordError() {
        return oldPasswordError;
    }

    public void setOldPasswordError(String oldPasswordError) {
        this.oldPasswordError = oldPasswordError;
        notifyPropertyChanged(BR.passwordError);
    }

    public interface OnChangePasswordListener {
        void onSaveClick(String oldPassword, String newPassword);
    }
}

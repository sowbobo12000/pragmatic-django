package com.fresconews.fresco.v2.settings;

import android.app.Dialog;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableString;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.persistence.models.Session;
import com.fresconews.fresco.v2.settings.dialogs.ChangeEmailDialogViewModel;
import com.fresconews.fresco.v2.settings.dialogs.ChangePasswordDialogViewModel;
import com.fresconews.fresco.v2.settings.dialogs.ChangeUserNameDialogViewModel;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;

import rx.functions.Action1;

/**
 * Created by mauricewu on 10/28/16.
 */
public class SettingsMeViewModel extends BaseSettingsViewModel {

    public BindableString userName = new BindableString();
    public BindableString userEmail = new BindableString();

    private ChangeEmailDialogViewModel changeEmailDialogViewModel;
    private ChangePasswordDialogViewModel changePasswordDialogViewModel;
    private ChangeUserNameDialogViewModel changeUserNameDialogViewModel;

    public SettingsMeViewModel(ActivityViewModel activityViewModel) {
        super(activityViewModel);

        getUser().onErrorReturn(throwable -> null).subscribe(user -> {
            if (user == null) {
                return;
            }
            setUserName(user.getUsername());
            setUserEmail(user.getEmail());
        });
    }

    public Action1<View> showUserNameChangeDialog = view -> {
        showChangeUserNamePopUp(userName.get());
    };

    public Action1<View> showEmailChangeDialog = view -> {
        showChangeEmailPopUp(userEmail.get());
    };

    public Action1<View> showPasswordChangeDialog = view -> {
        showChangePasswordPopUp();
    };

    public void setUserName(String userName) {
        this.userName.set(userName);
    }

    public void setUserEmail(String email) {
        this.userEmail.set(email);
    }

    private void showChangeEmailPopUp(String email) {
        if (changeEmailDialogViewModel == null) {
            changeEmailDialogViewModel = new ChangeEmailDialogViewModel(getActivity(), email, (email1, password) ->
                    userManager.setEmail(email1, password)
                               .onErrorReturn(throwable -> {
                                   SnackbarUtil.retrySnackbar(getRoot(), R.string.error_updating_email,
                                           view -> showChangeEmailPopUp(email1));
                                   return null;
                               })
                               .filter(networkSettings -> networkSettings != null)
                               .subscribe(networkSettings -> {//todo does this still get triggered
                                   userEmail.set(email1);
                                   getUser().onErrorReturn(throwable -> null).subscribe(user -> {
                                       if (user != null) {
                                           user.setEmail(email1);
                                           user.save();
                                       }
                                   });
                               }));
        }
        else {
            changeEmailDialogViewModel.reload(email);
        }

        if (!changeEmailDialogViewModel.isShowing()) {
            changeEmailDialogViewModel.show(R.layout.view_change_email);
        }
        TextInputLayout passwordLayout = (TextInputLayout) changeEmailDialogViewModel.getDialog().findViewById(R.id.password_text_input_layout);
    }

    private void showChangePasswordPopUp() {
        if (changePasswordDialogViewModel == null) {
            changePasswordDialogViewModel = new ChangePasswordDialogViewModel(getActivity(), (oldPassword, newPassword) ->
                    userManager.setPassword(newPassword, oldPassword)
                               .onErrorReturn(throwable -> {
                                   SnackbarUtil.retrySnackbar(getRoot(), R.string.error_updating_password,
                                           view -> showChangePasswordPopUp());
                                   return null;
                               })
                               .subscribe(networkSettings -> {
                                   if (networkSettings == null) {
                                       SnackbarUtil.retrySnackbar(getRoot(), R.string.error_updating_password,
                                               view -> showChangePasswordPopUp());
                                   }
                               }));
        }
        else {
            changePasswordDialogViewModel.reload();
        }

        if (!changePasswordDialogViewModel.isShowing()) {
            changePasswordDialogViewModel.show(R.layout.view_change_password);
        }
    }

    private void showChangeUserNamePopUp(String username) {
        if (changeUserNameDialogViewModel == null) {
            changeUserNameDialogViewModel = new ChangeUserNameDialogViewModel(getActivity(), username, (username1, password) ->
                    userManager.setUsername(username1, password)
                               .onErrorReturn(throwable -> {
                                   SnackbarUtil.retrySnackbar(getRoot(), R.string.error_updating_username,
                                           view -> showChangeUserNamePopUp(username1));
                                   return null;
                               })
                               .subscribe(networkSettings -> {

                                   if (networkSettings != null) {
                                       userName.set(username1);
                                       //Update session manager
                                       Session currentSession = sessionManager.getOrCreateSession();
                                       if (currentSession != null) {
                                           currentSession.setUsername(username1);
                                       }
                                       sessionManager.setCurrentSession(currentSession);
                                       //Save info locally too
                                       getUser().onErrorReturn(throwable -> null).subscribe(user -> {
                                           if (user != null) {
                                               user.setUsername(username1);
                                               user.save();
                                           }
                                       });
                                       activityViewModel.refreshHeader(currentSession);
                                   }
                                   else {
                                       SnackbarUtil.retrySnackbar(getRoot(), R.string.error_updating_username,
                                               view -> showChangeUserNamePopUp(username1));
                                   }
                               }));
        }
        else {
            changeUserNameDialogViewModel.reload(username);
        }

        if (!changeUserNameDialogViewModel.isShowing()) {
            changeUserNameDialogViewModel.show(R.layout.view_change_user_name);
        }
    }
}

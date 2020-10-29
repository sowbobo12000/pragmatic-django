package com.fresconews.fresco.v2.login;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.Bindable;
import android.text.TextUtils;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableString;
import com.fresconews.fresco.framework.mvvm.ViewModel;
import com.fresconews.fresco.framework.network.responses.NetworkSuccessResult;
import com.fresconews.fresco.framework.persistence.managers.AuthManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.Session;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.v2.home.HomeActivity;
import com.fresconews.fresco.v2.mediabrowser.MediaItemViewModel;
import com.fresconews.fresco.v2.submission.SubmissionActivity;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.fresconews.fresco.v2.utils.StringUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Maurice on 06/09/2016.
 */
public class MigrateUserDialogViewModel extends ViewModel {

    @Inject
    AuthManager authManager;

    @Inject
    SessionManager sessionManager;

    @Inject
    UserManager userManager;

    public BindableString newUsername = new BindableString();
    public BindableString newEmail = new BindableString();
    public BindableString newPassword = new BindableString();

    private Activity activity;
    private View activityRootView;
    private View dialogView;
    private Dialog dialog;
    private User user;
    private String message;
    private String password;
    private String platform;
    private String token;
    private String secret;
    private String usernameError;
    private String emailError;
    private String passwordError;
    private boolean hasUsername;
    private boolean hasPassword;
    private boolean hasEmail;

    private ArrayList<MediaItemViewModel> mediaItems;

    MigrateUserDialogViewModel(Activity activity, View activityRootView, User user, String password, ArrayList<MediaItemViewModel> mediaItems) {
        this(activity, activityRootView, user, password, null, null, null, mediaItems);
    }

    MigrateUserDialogViewModel(Activity activity, View activityRootView, User user, String password, String platform, String token, String secret, ArrayList<MediaItemViewModel> mediaItems) {
        this.activity = activity;
        this.activityRootView = activityRootView;
        this.user = user;
        this.password = password;
        this.platform = platform;
        this.token = token;
        this.secret = secret;
        newUsername.set(user == null ? "" : user.getUsername());
        newEmail.set(user == null ? "" : user.getEmail());
        this.mediaItems = mediaItems;

        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        if (user != null) {
            hasEmail = !TextUtils.isEmpty(user.getEmail());
            hasUsername = !TextUtils.isEmpty(user.getUsername());
            hasPassword = user.isValidPassword();
        }

        setMessageWrapper();
    }

    @Override
    public void onBound() {
        super.onBound();
        setMessageWrapper();
    }

    private void setMessageWrapper() {
        if (!hasUsername && !hasEmail && !hasPassword) {
            setMessage(activity.getString(R.string.migrate_user_message_username_email_and_password));
        }
        else if (!hasUsername && !hasEmail) {
            setMessage(activity.getString(R.string.migrate_user_message_username_and_email));
        }
        else if (!hasUsername && !hasPassword) {
            setMessage(activity.getString(R.string.migrate_user_message_username_and_password));
        }
        else if (!hasEmail && !hasPassword) {
            setMessage(activity.getString(R.string.migrate_user_message_email_and_password));
        }
        else if (!hasUsername) {
            setMessage(activity.getString(R.string.migrate_user_message_username));
        }
        else if (!hasEmail) {
            setMessage(activity.getString(R.string.migrate_user_message_email));
        }
        else if (!hasPassword) {
            setMessage(activity.getString(R.string.migrate_user_message_password));
        }
    }

    public void show(View root) {
        if (dialog == null) {
            dialog = new Dialog(activity);
            dialogView = root;
            dialog.setContentView(root);
            dialog.setCancelable(false);
        }
        dialog.show();
    }

    @Bindable
    public boolean isHasPassword() {
        return hasPassword;
    }

    @Bindable
    public boolean isHasEmail() {
        return hasEmail;
    }

    @Bindable
    public boolean isHasUsername() {
        return hasUsername;
    }

    public Action1<View> logOut = view -> {
        authManager.logout();
//        HomeActivity.start(activity, true);
        ((LoginActivity) activity).killMe();
        dialog.dismiss();
    };

    private Observable<Boolean> isValid() {
        boolean valid = true;

        String usernameFiltered = StringUtils.filterUserName(newUsername.get());
        if (usernameFiltered.trim().equals("") && !hasUsername) {
            valid = false;
            setUsernameError(activity.getString(R.string.error_username_empty));
        }
        else {
            setUsernameError("");
        }

        if (!StringUtils.isEmailValid(newEmail.get()) && !hasEmail) {
            valid = false;
            setEmailError(activity.getString(R.string.error_email_invalid));
        }
        else {
            setEmailError("");
        }

        if (TextUtils.isEmpty(newPassword.get().trim()) && !hasPassword) {
            valid = false;
            setPasswordError(activity.getString(R.string.error_password_empty));
        }
        else {
            setPasswordError("");
        }

        if (!valid) {
            return Observable.just(false);
        }

        return userManager.checkUsernameAndEmail(usernameFiltered,newEmail.get().trim())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
                    return null;
                })
                .flatMap(networkUserCheck -> {
                    boolean available = true;
                    if(networkUserCheck == null){
                        available = false;
                        return Observable.just(available);
                    }
                    if(!networkUserCheck.isAvailable()){
                        available = false;
                        String[] fieldsUnavailable = networkUserCheck.getFieldsUnavailable();
                        for(int i=0; i < fieldsUnavailable.length; i++){
                            String field = fieldsUnavailable[i];
                            if(field.equals("username")){
                                setUsernameError(activity.getString(R.string.error_username_exists));
                            }
                            else if(field.equals("email")){
                                setEmailError(activity.getString(R.string.error_email_exists));
                            }
                        }
                    }
                    return Observable.just(available);
                });

//        return Observable.zip(
//                userManager.checkUsername(usernameFiltered), //what if no username
//                userManager.checkEmail(newEmail.get()), //what if no email in the dialog
//                (user, email) -> {
//                    boolean available = true;
//
//                    if (user != null && !hasUsername) {
//                        setUsernameError(activity.getString(R.string.error_username_exists));
//                        available = false;
//                    }
//                    if (email != null && !hasEmail) {
//                        setEmailError(activity.getString(R.string.error_email_exists));
//                        available = false;
//                    }
//
//                    return available;
//                });
    }

    public Action1<View> done = view -> {
        if (!hasPassword) {
            password = newPassword.get();
        }
        password = StringUtils.toNullIfEmpty(password);

        if (hasEmail) {
            newEmail.set(null);
        }
        if (hasUsername) {
            newUsername.set(null);
        }

        isValid().onErrorReturn(throwable -> false).subscribe(valid -> {
            if (valid) {
                Observable<NetworkSuccessResult> call;
                if (TextUtils.isEmpty(platform)) {
                    call = userManager.setUsernameEmail(StringUtils.toNullIfEmpty(newUsername.get()), StringUtils.toNullIfEmpty(newEmail.get()), password);
                }
                else {
                    call = userManager.setSocialUsernameEmail(StringUtils.toNullIfEmpty(newUsername.get()), StringUtils.toNullIfEmpty(newEmail.get()), password, platform, token, secret); //Social login
                    //idk if you can call all three of these separately like this -- handedl it. added null handling
                }
                call.onErrorReturn(throwable -> {
                    if (throwable instanceof HttpException) {
                        try {
                            String body = ((HttpException) throwable).response().errorBody().string();
                            NetworkSuccessResult result = new Gson().fromJson(body, NetworkSuccessResult.class);
                            if (dialogView != null && result != null && result.getError() != null && result.getError().getMsg() != null) {
                                SnackbarUtil.dismissableSnackbar(dialogView, result.getError().getMsg());
                            }
                        }
                        catch (IOException e) {
                            if (activityRootView != null) {
                                SnackbarUtil.dismissableSnackbar(dialogView, activity.getString(R.string.error_default));
                            }
                        }
                    }
                    return null;
                }).onErrorReturn(throwable -> null).subscribe(networkSuccessResult -> {
                    if (networkSuccessResult != null) {
                        if (dialog != null) {
                            dialog.dismiss();
                            ((LoginActivity) activity).killMe();
                        }
                        userManager.downloadUser(user.getId())
                                   .subscribeOn(Schedulers.io())
                                   .observeOn(AndroidSchedulers.mainThread())
                                   .onErrorReturn(throwable -> null)
                                   .subscribe(user -> {
                                       if (user != null) {
                                           Session session = sessionManager.getCurrentSession();
                                           if (session != null) {
                                               session.setUserId(user.getId());
                                               session.setUsername(user.getUsername());
                                               sessionManager.setCurrentSession(session);
                                           }
                                           user.setValidPassword(true);
                                           userManager.setSmoochUser(user);
                                           Fresco2.startLocationService();
//                                           HomeActivity.start(activity, true);
                                           ((LoginActivity) activity).killMe();
                                           dialog.dismiss();
                                       }
                                   });
                    }
                });
            }
        });
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

    @Bindable
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        notifyPropertyChanged(BR.message);
    }


}

package com.fresconews.fresco.v2.editprofile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.Bindable;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableBoolean;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableString;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.network.requests.NetworkSignupRequest;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.AuthManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.v2.utils.ContentUtils;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.google.gson.Gson;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.fresconews.fresco.v2.editprofile.EditProfileActivity.EXTRA_AVATAR;
import static com.fresconews.fresco.v2.editprofile.EditProfileActivity.EXTRA_FULL_NAME;
import static com.fresconews.fresco.v2.editprofile.EditProfileActivity.EXTRA_NETWORK_REQUEST;
import static com.fresconews.fresco.v2.editprofile.EditProfileActivity.EXTRA_NOTIFICATION_RADIUS;
import static com.fresconews.fresco.v2.editprofile.EditProfileActivity.EXTRA_USER_ID;

/**
 * Created by Blaze on 7/18/2016.
 */
public class EditProfileViewModel extends ActivityViewModel<EditProfileActivity> {
    private static final String TAG = EditProfileViewModel.class.getSimpleName();
    private static final int KILL_ME = 666;

    public BindableString fullname = new BindableString();
    public BindableString location = new BindableString();
    public BindableString bio = new BindableString();
    public BindableBoolean saving = new BindableBoolean();

    private User user;
    private String avatarUrl;
    private String fullnameError;
    private boolean controlsEnabled = true;
    private boolean avatarFromSocial = false;
    private boolean signupUser;

    @Inject
    AuthManager authManager;

    @Inject
    UserManager userManager;

    @Inject
    AnalyticsManager analyticsManager;

    EditProfileViewModel(EditProfileActivity activity) {
        super(activity);

        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);
        setNavIcon(R.drawable.ic_close_gray);

        String userId = getActivity().getIntent().getStringExtra(EXTRA_USER_ID);

        // If userId is null, it means we are signing up, otherwise we are editing
        setSignupUser(TextUtils.isEmpty(userId));

        // Check if avatar is set from signup
        String avatar = getActivity().getIntent().getStringExtra(EXTRA_AVATAR);
        if (!TextUtils.isEmpty(avatar)) {
            setAvatarUrl(avatar);
            avatarFromSocial = true;
        }

        if (getActivity().getIntent() != null && !TextUtils.isEmpty(getActivity().getIntent().getStringExtra(EXTRA_FULL_NAME))) {
            fullname.set(getActivity().getIntent().getStringExtra(EXTRA_FULL_NAME));
        }

        // Load user to edit
        if (!TextUtils.isEmpty(userId)) {
            userManager.getUser(userId)
                       .onErrorReturn(throwable -> {
                           return null;
                       })
                       .subscribe(user -> {
                           this.user = user;
                           if (!TextUtils.isEmpty(user.getBio())) {
                               bio.set(user.getBio().trim());
                           }
                           if (!TextUtils.isEmpty(user.getFullName())) {
                               fullname.set(user.getFullName());
                           }
                           if (!TextUtils.isEmpty(user.getLocation())) {
                               location.set(user.getLocation());
                           }
                           if (!TextUtils.isEmpty(user.getAvatar())) {
                               setAvatarUrl(user.getAvatar());
                               notifyPropertyChanged(BR.avatarUrl);
                           }
                       });
        }
    }

    private boolean isValid() {
        return true;
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
    public boolean isSignupUser() {
        return signupUser;
    }

    public void setSignupUser(boolean signupUser) {
        this.signupUser = signupUser;
        notifyPropertyChanged(BR.signupUser);
    }

    @Bindable
    public String getFullnameError() {
        return fullnameError;
    }

    public void setFullnameError(String fullnameError) {
        this.fullnameError = fullnameError;
        notifyPropertyChanged(BR.fullnameError);
    }

    @Bindable
    public boolean getControlsEnabled() {
        return controlsEnabled;
    }

    private void setControlsEnabled(boolean enabled) {
        controlsEnabled = enabled;
        notifyPropertyChanged(BR.controlsEnabled);
    }

    public Action1<View> signup = view -> {
        setControlsEnabled(false);
        if (!isValid()) {
            setControlsEnabled(true);
            return;
        }

        String requestString = getActivity().getIntent().getStringExtra(EXTRA_NETWORK_REQUEST);
        NetworkSignupRequest request = new Gson().fromJson(requestString, NetworkSignupRequest.class);

        double notificationRadius = getActivity().getIntent().getDoubleExtra(EXTRA_NOTIFICATION_RADIUS, 0);

        request.setFullname(fullname.get());
        request.setLocation(location.get());
        request.setBio(bio.get());

        authManager.register(request)
                   .subscribeOn(Schedulers.io())
                   .onErrorReturn(throwable -> null)
                   .subscribe(user -> {
                       if (user == null) {
                           SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_create_account);
                           setControlsEnabled(true);
                       }
                       else {
                           userManager.setNotificationRadius(notificationRadius);
                           analyticsManager.trackUser(user.getId());
                           analyticsManager.signedUpWithEmail();
                           analyticsManager.signupRadiusSet(notificationRadius);

                           if (!avatarFromSocial && getAvatarUrl() != null && !getAvatarUrl().equals("")) {
                               String path = ContentUtils.getMediaPath(getActivity(), Uri.parse(getAvatarUrl()));
                               if (path != null) {
                                   userManager.setAvatar(new File(path))
                                              .onErrorReturn(throwable -> {
                                                  setControlsEnabled(true);
                                                  return null;
                                              })
                                              .subscribe(uri -> {
                                                  if (uri != null) {
                                                      user.setAvatar(uri.toString());
                                                      user.save();
                                                  }
                                                  finishSignup(user);
                                              });
                               }
                               else {
                                   SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_image_pick);
                               }
                           }
                           else {
                               finishSignup(user);
                           }
                       }
                   });
    };

    public Action1<View> save = view -> {

        //Disable save button so user can't spam it and cause an out of memory error on the picture they're using
        saving.set(true);

        setControlsEnabled(false);
        if (!isValid()) {
            setControlsEnabled(true);
            return;
        }

        userManager.setUserProfile(fullname.get(), bio.get(), location.get())
                   .onErrorReturn(throwable -> {
                       LogUtils.i(TAG, "Error saving user");
                       SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_edit_account);
                       saving.set(false);
                       setControlsEnabled(true);
                       return null;
                   })
                   .subscribe(networkUser -> {
                       if (networkUser == null || user == null) {
                           LogUtils.i(TAG, "Error saving user");
                           SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_edit_account);
                           saving.set(false);
                           setControlsEnabled(true);
                       }
                       else {
                           user.setBio(bio.get());
                           user.setFullName(fullname.get());
                           user.setLocation(location.get());
                           user.save();

                           if (!avatarFromSocial && !TextUtils.isEmpty(getAvatarUrl()) && !getAvatarUrl().contains("http")) {
                               String path = null;
                               if (getAvatarUrl().startsWith("file://")) {
                                   path = getAvatarUrl().substring(8);
                               }
                               else {
                                   path = ContentUtils.getMediaPath(getActivity(), Uri.parse(getAvatarUrl()));
                               }
                               if (path != null) {
                                   userManager.setAvatar(new File(path))
                                              .onErrorReturn(throwable -> null)
                                              .flatMap(uri -> {
                                                  if (uri != null) {
                                                      user.setAvatar(uri.toString());
                                                      user.save();
                                                  }
                                                  else {
                                                      saving.set(false);
                                                      setControlsEnabled(true);
                                                  }
                                                  return Observable.just(uri);
                                              })
                                              .delay(1, TimeUnit.SECONDS)
                                              .onErrorReturn(throwable -> null)
                                              .subscribe(uri -> {
                                                  if (uri != null) {
                                                      Intent result = new Intent();
                                                      result.setData(Uri.parse(uri.toString()));
                                                      getActivity().setResult(Activity.RESULT_OK, result);
                                                      getActivity().finish();
                                                  }
                                                  else {
                                                      SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_image_pick);
                                                      saving.set(false);
                                                      setControlsEnabled(true);
                                                  }
                                              });
                               }
                               else {
                                   SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_image_pick);
                                   saving.set(false);
                                   setControlsEnabled(true);

                               }
                           }
                           else {
                               getActivity().setResult(Activity.RESULT_OK);
                               getActivity().finish();
                           }
                       }
                   });
    };

    private void finishSignup(User user) {
        userManager.setSmoochUser(user);
        Fresco2.startLocationService();
        getActivity().killMe();
    }

    public Action1<View> profilePictureClicked = view -> {
        if (!getControlsEnabled()) {
            return;
        }
        checkCameraPermissions();
    };

    private void checkCameraPermissions() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setView(R.layout.view_permissions)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EditProfileActivity.PERMISSIONS_STORAGE_REQUEST_CODE);
                            dialogInterface.dismiss();
                        })
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
                AlertDialog dialog = builder.create();
                dialog.setOnShowListener(dialogInterface -> {
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.black_87));
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.fresco_blue));
                });
                dialog.show();
            }
            else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EditProfileActivity.PERMISSIONS_STORAGE_REQUEST_CODE);
            }
        }
        else {
            getActivity().pickImage()
                         .onErrorReturn(throwable -> null)
                         .subscribe(this::setAvatarUrl);
        }
    }
}


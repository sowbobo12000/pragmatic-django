package com.fresconews.fresco.v2.login;

import android.content.Intent;
import android.databinding.Bindable;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableString;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.network.responses.NetworkTerms;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.AuthManager;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.framework.persistence.models.User_Table;
import com.fresconews.fresco.v2.home.HomeActivity;
import com.fresconews.fresco.v2.mediabrowser.MediaItemViewModel;
import com.fresconews.fresco.v2.submission.SubmissionActivity;
import com.fresconews.fresco.v2.utils.KeyboardUtils;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.NetworkUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.fresconews.fresco.v2.utils.StringUtils;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.twitter.sdk.android.core.TwitterAuthToken;

import java.util.ArrayList;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class LoginViewModel extends ActivityViewModel<LoginActivity> {
    private static final String TAG = LoginViewModel.class.getSimpleName();

    private final static String PLATFORM_FACEBOOK = "facebook";
    private final static String PLATFORM_TWITTER = "twitter";
    private final static String PLATFORM_GOOGLE = "google";
    private final static String FRESCO_PASSWORD_FORGOT_URL = "https://www.fresconews.com/forgot";

    @Inject
    AuthManager authManager;

    @Inject
    SessionManager mSessionManager;

    @Inject
    GalleryManager galleryManager;

    @Inject
    AnalyticsManager analyticsManager;

    @Inject
    UserManager userManager;

    public BindableString username = new BindableString();
    public BindableString password = new BindableString();

    private boolean controlsEnabled = true;
    private ArrayList<MediaItemViewModel> mediaItems;

    public LoginViewModel(LoginActivity activity) {
        super(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        setNavIcon(R.drawable.ic_navigation_arrow_back);
    }

    public LoginViewModel(LoginActivity activity, ArrayList<MediaItemViewModel> mediaItems) {
        super(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);
        this.mediaItems = mediaItems;

        setNavIcon(R.drawable.ic_navigation_arrow_back);
    }

    public Action1<View> login = view -> {
        KeyboardUtils.hideKeyboard(getActivity());

        if (TextUtils.isEmpty(username.get().trim())) {
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_username_email_empty);
        }
        else if (TextUtils.isEmpty(password.get().trim())) {
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_password_empty);
        }

        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_check_internet);
            return;
        }

        setControlsEnabled(false);
        authManager.signInNewAuth(StringUtils.filterUserName(username.get()), password.get())
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .onErrorReturn(throwable -> null)
                   .subscribe(user -> {
                       setControlsEnabled(true);
                       if (user == null) {
                           SnackbarUtil.dismissableSnackbar(getRoot(), R.string.login_incorrect);
                       }
                       else {
                           handleLogin(user, null, null, null);
                       }
                   });
    };

    public Action1<View> loginTwitter = view -> {
        getActivity().getTwitterSession()
                     .onErrorReturn(throwable -> null)
                     .subscribe(twitterSession -> {
                         if (twitterSession != null) {
                             TwitterAuthToken authToken = twitterSession.getAuthToken();
                             authManager.signInTwitter(authToken).subscribe(user -> {
                                 if (user == null) {
                                     SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_twitter_login);
                                 }
                                 else {
                                     handleLogin(user, PLATFORM_TWITTER, authToken.token, authToken.secret);
                                 }
                             });
                         }
                         else {
                             SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_twitter_login);
                         }
                     });
    };

    public Action1<View> loginFacebook = view -> {
        getActivity().getFacebookSession()
                     .onErrorReturn(throwable -> null)
                     .subscribe(accessToken -> {
                         if (accessToken == null) {
                             SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_facebook_login);
                         }

                         authManager.signInFacebook(accessToken).onErrorReturn(throwable -> null).subscribe(user -> {
                             if (user == null) {
                                 SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_facebook_login);
                             }
                             else {
                                 handleLogin(user, PLATFORM_FACEBOOK, accessToken.getToken(), null);
                             }
                         });

                     });
    };

    public Action1<View> loginGoogle = view -> {
        getActivity().beginGoogleLogin();

    };

    public void loginGoogleCompelted(GoogleSignInResult result) {

//        authManager.signInGoogle(result.)

        GoogleSignInAccount acct = null;
        if (result != null && result.isSuccess() && result.getSignInAccount() != null && result.getSignInAccount().getIdToken() != null) {
            acct = result.getSignInAccount();
            LogUtils.i(TAG, "GOOGLE LOGIN SUCCESS!!!! - " + acct.getDisplayName());
        }
        else {
            LogUtils.e(TAG, "GOOGLE FAIL - " + result.getStatus());
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_google_login);
            return;
        }

        String authCode = acct.getServerAuthCode();
        String idToken = acct.getIdToken();
        LogUtils.i(TAG, "google server auth code - " + authCode);
        LogUtils.i(TAG, "google id token  - " + idToken);

        authManager.signInGoogle(idToken).onErrorReturn(throwable -> null).subscribe(user -> {
            if (user == null) {
                SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_google_login);
            }
            else {
                handleLogin(user, PLATFORM_GOOGLE, idToken, null);
            }
        });
    }

    private void handleLogin(User user, String platform, String token, String secret) {
        //Check for privacy policy updates
        NetworkTerms networkTerms = authManager.getNetworkTerms();
        String bodyText = networkTerms.getTerms();
        if (bodyText != null) {
            bodyText = bodyText.replace('�', '"');
        }

        if (networkTerms != null) {
            if (!networkTerms.isValid()) {
                final String finalBodyText = bodyText;
                getActivity().runOnUiThread(() -> {
                    TOSDialogViewModel tosDialogViewModel = new TOSDialogViewModel(getActivity(), finalBodyText, TOSDialogViewModel.UPDATED_TOS_TYPE, () -> finishLogin(user, platform, token, secret));
                    ViewDataBinding dataBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.view_tos, null, false);
                    dataBinding.setVariable(BR.model, tosDialogViewModel);
                    tosDialogViewModel.show(dataBinding.getRoot());
                });
            }
            else {
                finishLogin(user, platform, token, secret);
            }
        }
    }

    private void finishLogin(User user, String platform, String token, String secret) {
        galleryManager.clearGalleries();
        analyticsManager.loggedIn(user.getId(), platform);

        // Shows migration dialog for users coming from v1
        // Check if user has username for Fresco Email logins and check if user has a password for social logins
        if (TextUtils.isEmpty(user.getEmail()) || TextUtils.isEmpty(user.getUsername()) || !user.isValidPassword()) {
            getActivity().runOnUiThread(() -> {
                MigrateUserDialogViewModel migrateUserDialogViewModel;
                if (!(StringUtils.toNullIfEmpty(platform) == null)) {
                    if (mediaItems == null) {
                        migrateUserDialogViewModel =
                                new MigrateUserDialogViewModel(getActivity(), getActivity().getDataBinding().getRoot(), user, password.get(), platform, token, secret, null);
                    }
                    else {
                        migrateUserDialogViewModel =
                                new MigrateUserDialogViewModel(getActivity(), getActivity().getDataBinding().getRoot(), user, password.get(), platform, token, secret, mediaItems);
                    }
                }
                else {
                    migrateUserDialogViewModel =
                            new MigrateUserDialogViewModel(getActivity(), getActivity().getDataBinding().getRoot(), user, password.get(), mediaItems);
                }
                ViewDataBinding dataBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.view_migrate_user, null, false);
                dataBinding.setVariable(BR.model, migrateUserDialogViewModel);
                migrateUserDialogViewModel.show(dataBinding.getRoot());

            });
        }
        else {
            userManager.setSmoochUser(user);
            if (mediaItems == null) {
                getActivity().killMe(user);
            }
            else {
                SubmissionActivity.start(getActivity(), mediaItems);
            }
            Fresco2.startLocationService();
        }
    }

    public Action1<View> forgotPassword = view -> {
        if (NetworkUtils.isNetworkAvailable(getActivity())) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FRESCO_PASSWORD_FORGOT_URL));
            getActivity().startActivity(browserIntent);
        }
        else {
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_check_internet);
        }
    };

    @Bindable
    public boolean getControlsEnabled() {
        return controlsEnabled;
    }

    private void setControlsEnabled(boolean enabled) {
        controlsEnabled = enabled;
        notifyPropertyChanged(BR.controlsEnabled);
    }

    public Action1<View> goBack = view -> {
        ActivityCompat.finishAfterTransition(getActivity());
    };
}
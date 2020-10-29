package com.fresconews.fresco.v2.settings;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.network.EndpointHelper;
import com.fresconews.fresco.framework.network.responses.NetworkSuccessResult;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.v2.signup.SignupViewModel;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.gson.Gson;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.io.IOException;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by mauricewu on 11/3/16.
 */
public class SettingsSocialViewModel extends BaseSettingsViewModel implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = SettingsSocialViewModel.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;

    public BindableView<SwitchCompat> facebookSwitch = new BindableView<>();
    public BindableView<SwitchCompat> twitterSwitch = new BindableView<>();
    public BindableView<SwitchCompat> googleSwitch = new BindableView<>();

    private boolean isTwitterTouched = false;
    private boolean isFacebookTouched = false;
    private boolean isGoogleTouched = false;
    private GoogleSignInAccount acct = null;
    private String googleIdToken;
    private CallbackManager fbCallbackManager;
    private TwitterAuthClient authClient;
    private GoogleApiClient googleApiClient;
    private GoogleSignInOptions gso;

    public SettingsSocialViewModel(ActivityViewModel activityViewModel) {
        super(activityViewModel);

        fbCallbackManager = CallbackManager.Factory.create();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //.requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestScopes(new Scope(Scopes.PLUS_ME))
                //.requestScopes(new Scope("https://www.googleapis.com/auth/userinfo.profile"))
                .requestIdToken(EndpointHelper.currentEndpoint.googleOauthServerCredentialId)
                .requestServerAuthCode(EndpointHelper.currentEndpoint.googleOauthServerCredentialId)
                .requestEmail()
                .build();
    }

    @Override
    public void onBound() {
        super.onBound();
        bindSwitches();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from
        LogUtils.i(TAG, "google gone hore ee ah");

        if (requestCode == RC_SIGN_IN) {
            LogUtils.i(TAG, "google result code - " + Integer.toString(resultCode));

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            loginGoogleCompelted(result);
        }
    }

    public TwitterAuthClient getTwitterAuthClient() {
        if (authClient == null) {
            synchronized (SettingsActivity.class) {
                if (authClient == null) {
                    authClient = new TwitterAuthClient();
                }
            }
        }
        return authClient;
    }

    private void setSocialSwitches(User user) {
        facebookSwitch.get().setChecked(!TextUtils.isEmpty(user.getFacebookSocialLink()));
        twitterSwitch.get().setChecked(!TextUtils.isEmpty(user.getTwitterSocialLink()));
        googleSwitch.get().setChecked(!TextUtils.isEmpty(user.getGoogleSocialLink()));
    }

    private void bindSwitches() {
        getUser().observeOn(AndroidSchedulers.mainThread())
                 .subscribeOn(Schedulers.io())
                 .onErrorReturn(throwable -> null)
                 .subscribe(user -> {
                     if (user != null) {
                         setSocialSwitches(user);
                     }
                 });

        twitterSwitch.get().setOnTouchListener((view, motionEvent) -> {
            isTwitterTouched = true;
            return false;
        });

        twitterSwitch.get().setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isTwitterTouched) {
                isTwitterTouched = false;
                if (!isChecked) {
                    userManager.disconnectSocial("twitter")
                               .observeOn(AndroidSchedulers.mainThread())
                               .subscribeOn(Schedulers.io())
                               .onErrorReturn(throwable -> {
                                   getUser().observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .onErrorReturn(throwable2 -> null)
                                            .subscribe(user -> {
                                                if (user != null) {
                                                    setSocialSwitches(user);
                                                }
                                            });
                                   twitterSwitch.get().setChecked(true);
                                   SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_twitter_disconnect);
                                   return null;
                               })
                               .subscribe();
                }
                else {
                    getTwitterSession().observeOn(AndroidSchedulers.mainThread())
                                       .onErrorReturn(throwable1 -> {
                                           twitterSwitch.get().setChecked(false);
                                           return null;
                                       })
                                       .subscribe(twitterSession1 -> {
                                           if (twitterSession1 == null) {
                                               twitterSwitch.get().setChecked(false);
                                               SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_twitter_connect);
                                               twitterSwitch.get().setChecked(false);
                                           }
                                           else {
                                               TwitterAuthToken authToken = twitterSession1.getAuthToken();
                                               if (authToken == null) {
                                                   twitterSwitch.get().setChecked(false);
                                                   SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_twitter_connect);
                                               }
                                               else {
                                                   userManager.connectSocialTwitter(authToken.token, authToken.secret)
                                                              .observeOn(AndroidSchedulers.mainThread())
                                                              .subscribeOn(Schedulers.io())
                                                              .onErrorReturn(throwable -> {
                                                                  if (throwable instanceof HttpException) {
                                                                      if (((HttpException) throwable).response().code() == 412) {
                                                                          SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_twitter_already_linked);
                                                                      }
                                                                      else {
                                                                          SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_twitter_connect);
                                                                      }
                                                                  }
                                                                  else {
                                                                      SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_twitter_connect);
                                                                  }
                                                                  twitterSwitch.get().setChecked(false);
                                                                  return null;
                                                              })
                                                              .subscribe(networkUser -> {
                                                                  if (networkUser != null) {
                                                                      LogUtils.d(TAG, "Twitter connection successful");
                                                                  }
                                                                  else {
                                                                      LogUtils.d(TAG, "Twitter connection was unsuccessful");
                                                                      twitterSwitch.get().setChecked(false);
                                                                  }
                                                              });
                                               }
                                           }
                                       });
                }
            }
        });

        facebookSwitch.get().setOnTouchListener((view, motionEvent) -> {
            isFacebookTouched = true;
            return false;
        });

        facebookSwitch.get().setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isFacebookTouched) {
                isFacebookTouched = false;
                if (!isChecked) {
                    userManager.disconnectSocial("facebook")
                               .observeOn(AndroidSchedulers.mainThread())
                               .subscribeOn(Schedulers.io())
                               .onErrorReturn(throwable -> {
                                   getUser().observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .onErrorReturn(throwable2 -> null)
                                            .subscribe(user -> {
                                                if (user != null) {
                                                    setSocialSwitches(user);
                                                }
                                            });
                                   facebookSwitch.get().setChecked(true);
                                   SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_facebook_disconnect);
                                   return null;
                               })
                               .subscribe();
                }
                else {
                    getFacebookSession().observeOn(AndroidSchedulers.mainThread())
                                        .onErrorReturn(throwable1 -> {
                                            LogUtils.d(TAG, "Facebook failed to connect - " + throwable1.getMessage());
                                            facebookSwitch.get().setChecked(false);
                                            return null;
                                        })
                                        .subscribe(accessToken -> {
                                            if (accessToken == null) {
                                                LogUtils.d(TAG, "Auth token was null");
                                                facebookSwitch.get().setChecked(false);
                                                SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_facebook_connect);
                                                facebookSwitch.get().setChecked(false);
                                            }
                                            else {
                                                LogUtils.d(TAG, "Token " + accessToken.getToken());
                                                userManager.connectSocialFacebook(accessToken.getToken())
                                                           .observeOn(AndroidSchedulers.mainThread())
                                                           .subscribeOn(Schedulers.io())
                                                           .onErrorReturn(throwable -> {
                                                               if (throwable instanceof HttpException) {
                                                                   if (((HttpException) throwable).response().code() == 412) {
                                                                       SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_facebook_already_linked);
                                                                   }
                                                                   else {
                                                                       SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_facebook_connect);
                                                                   }
                                                               }
                                                               else {
                                                                   SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_facebook_connect);
                                                               }
                                                               facebookSwitch.get().setChecked(false);
                                                               return null;
                                                           }).subscribe();
                                            }
                                        });
                }
            }
        });

        googleSwitch.get().setOnTouchListener((view, motionEvent) -> {
            isGoogleTouched = true;
            return false;
        });

        googleSwitch.get().setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isGoogleTouched) {
                isGoogleTouched = false;
                if (!isChecked) {
                    userManager.disconnectSocial("google")
                               .observeOn(AndroidSchedulers.mainThread())
                               .subscribeOn(Schedulers.io())
                               .onErrorReturn(throwable -> {
                                   getUser().observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .onErrorReturn(throwable2 -> null)
                                            .subscribe(user -> {
                                                if (user != null) {
                                                    setSocialSwitches(user);
                                                }
                                            });
                                   googleSwitch.get().setChecked(true);
                                   SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_google_disconnect);
                                   return null;
                               })
                               .subscribe();
                }
                else {
                    beginGoogleLogin();
                }
            }
        });
    }

    public void beginGoogleLogin() {

        // Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .enableAutoManage(getActivity(), this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        getActivity().startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void loginGoogleCompelted(GoogleSignInResult googleResult) {

        if (googleResult != null && googleResult.isSuccess() && googleResult.getSignInAccount() != null && googleResult.getSignInAccount().getIdToken() != null) {
            acct = googleResult.getSignInAccount();
            LogUtils.i(TAG, "GOOGLE LOGIN SUCCESS!!!! - " + acct.getDisplayName());
        }
        else {
            LogUtils.e(TAG, "GOOGLE FAIL - " + googleResult.getStatus());
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_google_login);
            googleSwitch.get().setChecked(false);
            return;
        }

        String authCode = acct.getServerAuthCode();
        String idToken = acct.getIdToken();
        LogUtils.i(TAG, "google server auth code - " + authCode);
        LogUtils.i(TAG, "google id token  - " + idToken);
        LogUtils.i(TAG, "google email  - " + acct.getEmail());

        googleIdToken = acct.getIdToken();

        userManager.connectSocialGoogle(idToken)
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribeOn(Schedulers.io())
                   .onErrorReturn(throwable -> {
                       if (throwable instanceof HttpException) {
                           if (((HttpException) throwable).response().code() == 412) {
                               SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_google_already_linked);
                           }
                           else {
                               SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_google_connect);
                           }
                       }
                       else {
                           SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_google_connect);
                       }
                       googleSwitch.get().setChecked(false);
                       return null;
                   }).subscribe();

    }

    private Observable<AccessToken> getFacebookSession() {
        return Observable.create(subscriber -> {
            if (AccessToken.getCurrentAccessToken() != null) {
                subscriber.onNext(AccessToken.getCurrentAccessToken());
                subscriber.onCompleted();
                return;
            }

            LoginManager.getInstance().registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    subscriber.onNext(loginResult.getAccessToken());
                    subscriber.onCompleted();
                }

                @Override
                public void onCancel() {
                    facebookSwitch.get().setChecked(false);
                    LogUtils.w(TAG, "Facebook login cancel");
                    subscriber.onCompleted();
                }

                @Override
                public void onError(FacebookException error) {
                    facebookSwitch.get().setChecked(false);
                    LogUtils.e(TAG, "Facebook Error", error);
                    subscriber.onCompleted();
                }
            });
            LoginManager.getInstance().logInWithReadPermissions(getActivity(), null);
        });
    }

    private Observable<TwitterSession> getTwitterSession() {
        return Observable.create(subscriber -> {
            getTwitterAuthClient().authorize(getActivity(), new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    subscriber.onNext(result.data);
                    subscriber.onCompleted();
                }

                @Override
                public void failure(TwitterException exception) {
                    twitterSwitch.get().setChecked(false);
                    LogUtils.e(TAG, "Twitter Error", exception);
                    subscriber.onCompleted();
                }
            });
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //todo snackbar
    }
}

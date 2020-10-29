package com.fresconews.fresco.v2.signup;

import android.content.Intent;
import android.databinding.Bindable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.Profile;
import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableBoolean;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableString;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.network.requests.NetworkSignupRequest;
import com.fresconews.fresco.framework.network.responses.NetworkSuccessResult;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.AuthManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.v2.editprofile.EditProfileActivity;
import com.fresconews.fresco.v2.mediabrowser.MediaItemViewModel;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.NetworkUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.fresconews.fresco.v2.utils.StringUtils;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.gson.Gson;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import javax.inject.Inject;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SignupViewModel extends ActivityViewModel<SignupActivity> {
    private static final String TAG = SignupViewModel.class.getSimpleName();
    private static final String TERMS_URL = "https://www.fresconews.com/terms";
    private static final int KILL_ME = 666;

    @Inject
    AuthManager authManager;

    @Inject
    UserManager userManager;

    @Inject
    AnalyticsManager analyticsManager;

    public BindableString username = new BindableString();
    public BindableString emailAddress = new BindableString();
    public BindableString password = new BindableString();
    public BindableBoolean twitterLinked = new BindableBoolean();

    private String fullname;
    private String location;
    private String avatar;

    private TwitterAuthToken twitterToken;
    private AccessToken facebookToken;
    private GoogleSignInAccount acct = null;
    private String googleIdToken;

    private String usernameError;
    private String emailError;
    private String passwordError;

    private String notificationRadiusLabel;
    private double notificationRadiusMiles;

    private boolean controlsEnabled = false;

    private ArrayList<MediaItemViewModel> mediaItems;

    private boolean successfullyLinkedToTwitter;
    private boolean successfullyLinkedToFacebook;
    private boolean successfullyLinkedToGoogle;

    public SignupViewModel(SignupActivity activity) {
        super(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        setNavIcon(R.drawable.ic_navigation_arrow_back);
        notificationRadiusMiles = 30;
//        setNotificationRadiusLabel(String.format(Locale.getDefault(), "%.2f mi.", notificationRadiusMiles));
        setNotificationRadiusLabel(String.format(Locale.getDefault(), "%d mi.", (int) notificationRadiusMiles));

        getActivity().setNotificationRadius(notificationRadiusMiles);

    }

    private Observable<Boolean> isValid() {
        boolean valid = true;

        String usernameFiltered = StringUtils.filterUserName(username.get());
        if (usernameFiltered.trim().equals("")) {
            valid = false;
            setUsernameError(getActivity().getString(R.string.error_username_empty));
        }
        else {
            setUsernameError("");
        }

        if (!StringUtils.isEmailValid(emailAddress.get())) {
            valid = false;
            setEmailError(getActivity().getString(R.string.error_email_invalid));
        }
        else {
            setEmailError("");
        }

        if (TextUtils.isEmpty(password.get().trim())) {
            valid = false;
            setPasswordError(getActivity().getString(R.string.error_password_empty));
        }
        else {
            setPasswordError("");
        }

        if (!valid) {
            return Observable.just(false);
        }

        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_check_internet);
            return Observable.just(false);
        }

        return userManager.checkUsernameAndEmail(usernameFiltered, emailAddress.get().trim())
                          .observeOn(AndroidSchedulers.mainThread())
                          .onErrorReturn(throwable -> {
                              SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_create_account);
                              return null;
                          })
                          .flatMap(networkUserCheck -> {
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
                                          setUsernameError(getActivity().getString(R.string.error_username_exists));
                                      }
                                      else if (field.equals("email")) {
                                          setEmailError(getActivity().getString(R.string.error_email_exists));
                                      }
                                  }
                              }

                              return Observable.just(available);
                          });
    }

    @Bindable
    public boolean isSuccessfullyLinkedToTwitter() {
        return successfullyLinkedToTwitter;
    }

    public void setSuccessfullyLinkedToTwitter(boolean successfullyLinkedToTwitter) {
        this.successfullyLinkedToTwitter = successfullyLinkedToTwitter;
        notifyPropertyChanged(BR.successfullyLinkedToTwitter);
    }

    @Bindable
    public boolean isSuccessfullyLinkedToFacebook() {
        return successfullyLinkedToFacebook;
    }

    public void setSuccessfullyLinkedToFacebook(boolean successfullyLinkedToFacebook) {
        this.successfullyLinkedToFacebook = successfullyLinkedToFacebook;
        notifyPropertyChanged(BR.successfullyLinkedToFacebook);
    }

    @Bindable
    public boolean isSuccessfullyLinkedToGoogle() {
        return successfullyLinkedToGoogle;
    }

    public void setSuccessfullyLinkedToGoogle(boolean successfullyLinkedToGoogle) {
        LogUtils.i(TAG, "triggered google icon to - " + Boolean.toString(successfullyLinkedToGoogle));
        this.successfullyLinkedToGoogle = successfullyLinkedToGoogle;
        notifyPropertyChanged(BR.successfullyLinkedToGoogle);
    }

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
    public String getNotificationRadiusLabel() {
        return notificationRadiusLabel;
    }

    public void setNotificationRadiusLabel(String notificationRadiusLabel) {
        this.notificationRadiusLabel = notificationRadiusLabel;
        notifyPropertyChanged(BR.notificationRadiusLabel);
    }

    public Action1<View> toggleTOS = view -> {
        //TODO aaaaaand every other field has to be filled too
        if (StringUtils.toNullIfEmpty(username.get()) != null &&
                StringUtils.toNullIfEmpty(emailAddress.get()) != null &&
                StringUtils.toNullIfEmpty(password.get()) != null) {
            //TODO this wont work. You gotta listen on if they've filled them out
        }
        setControlsEnabled(!getControlsEnabled());

    };

    public Action1<View> goToTermsOfServices = view -> {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_URL));
        getActivity().startActivity(browserIntent);
    };

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
        isValid().onErrorReturn(throwable -> null).subscribe(valid -> {
            setControlsEnabled(true);
            if (valid == null || !valid) {
                return;
            }

            NetworkSignupRequest request;
            if (twitterToken != null) {
                LogUtils.i(TAG, "twatter token was good ");

                request = NetworkSignupRequest.fromTwitter(twitterToken);
            }
            else if (facebookToken != null) {
                LogUtils.i(TAG, "fb token was good ");

                request = NetworkSignupRequest.fromFacebook(facebookToken);
            }
            else if (googleIdToken != null) {
                LogUtils.i(TAG, "gargle token was good ");
                request = NetworkSignupRequest.fromGoogle(googleIdToken);

            }
            else {
                request = new NetworkSignupRequest();
            }
            request.setUsername(StringUtils.filterUserName(username.get()));
            request.setEmail(emailAddress.get());
            request.setPassword(password.get());

            EditProfileActivity.start(getActivity(), request, fullname, location, avatar, notificationRadiusMiles);
        });
    };

    public Action1<View> signupTwitter = view -> {
        getActivity().getTwitterSession().onErrorReturn(throwable -> null).subscribe(twitterSession -> {
            if (twitterSession == null || twitterSession.getAuthToken() == null) {
                SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_twitter_connect);
                setSuccessfullyLinkedToTwitter(false);
                return;
            }
            twitterToken = twitterSession.getAuthToken();
            username.set(twitterSession.getUserName());
            setSuccessfullyLinkedToTwitter(true);

            authManager.checkSocial("twitter", twitterToken.token, twitterToken.secret, null)
                       .observeOn(AndroidSchedulers.mainThread())
                       .subscribeOn(Schedulers.io())
                       .onErrorReturn(throwable -> {
                           if (throwable instanceof HttpException) {
                               if (((HttpException) throwable).response().code() == 412) {
                                   try {
                                       String body = ((HttpException) throwable).response().errorBody().string();
                                       NetworkSuccessResult result = new Gson().fromJson(body, NetworkSuccessResult.class);
                                       SnackbarUtil.dismissableSnackbar(getRoot(), result.getError().getMsg());
                                       setSuccessfullyLinkedToTwitter(false);
                                       twitterToken = null;
                                   }
                                   catch (IOException e) {
                                       twitterToken = null;
                                       setSuccessfullyLinkedToTwitter(false);
                                       LogUtils.i(TAG, "making twatter token null");
                                       SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_twitter_already_linked);
                                   }
                               }
                               else {
                                   twitterToken = null;
                                   setSuccessfullyLinkedToTwitter(false);
                                   SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_twitter_connect);
                               }
                           }
                           else {
                               twitterToken = null;
                               setSuccessfullyLinkedToTwitter(false);
                               SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_twitter_connect);
                           }
                           return null;
                       })
                       .subscribe(networkUser -> {
                           Twitter.getApiClient(twitterSession)
                                  .getAccountService()
                                  .verifyCredentials(true, false)
                                  .enqueue(new Callback<User>() {
                                      @Override
                                      public void success(Result<User> result) {
                                          if (twitterToken != null) {
                                              LogUtils.i(TAG, "setting up twitter to true");
                                              analyticsManager.signingUpWithTwitter();
                                              setSuccessfullyLinkedToTwitter(true);
                                          }
                                          else {
                                              setSuccessfullyLinkedToTwitter(false);
                                          }
                                          if (!TextUtils.isEmpty(result.data.email) && twitterToken != null) {
                                              emailAddress.set(result.data.email);
                                          }
                                          fullname = result.data.name;
                                          avatar = result.data.profileImageUrlHttps;
                                          location = result.data.location;

                                      }

                                      @Override
                                      public void failure(TwitterException exception) {
                                          exception.printStackTrace();
                                          setSuccessfullyLinkedToTwitter(false);
                                          twitterToken = null;
                                      }
                                  });
                       });

        });
    };

    public Action1<View> signupFacebook = view -> {
        getActivity().getFacebookSession().onErrorReturn(throwable -> null).subscribe(accessToken -> {
            facebookToken = accessToken;
            setSuccessfullyLinkedToFacebook(true);
            if(facebookToken == null){
                SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_facebook_connect);
                setSuccessfullyLinkedToFacebook(false);
                return;
            }
            LogUtils.i(TAG, "Facebook Token: " + accessToken.getToken());
            authManager.checkSocial("facebook", accessToken.getToken(), null, null)
                       .observeOn(AndroidSchedulers.mainThread())
                       .subscribeOn(Schedulers.io())
                       .onErrorReturn(throwable -> {
                           if (throwable instanceof HttpException) {
                               setSuccessfullyLinkedToFacebook(false);
                               if (((HttpException) throwable).response().code() == 412) {
                                   try {
                                       String body = ((HttpException) throwable).response().errorBody().string();
                                       NetworkSuccessResult result = new Gson().fromJson(body, NetworkSuccessResult.class);
                                       facebookToken = null;
                                       SnackbarUtil.dismissableSnackbar(getRoot(), result.getError().getMsg());
                                   }
                                   catch (IOException e) {
                                       //Delete fb token
                                       facebookToken = null;
                                       SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_facebook_already_linked);
                                   }
                               }
                               else {
                                   facebookToken = null;
                                   SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_facebook_connect);
                               }
                           }
                           else {
                               facebookToken = null;
                               setSuccessfullyLinkedToFacebook(false);
                               SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_facebook_connect);
                           }
                           return null;
                       })
                       .subscribe(networkAuthResponse -> {
                           GraphRequest request = GraphRequest.newMeRequest(accessToken, (object, response) -> {
                               if (facebookToken != null) {
                                   setSuccessfullyLinkedToFacebook(true);
                                   emailAddress.set(object.optString("email"));
                                   // TODO location is wrong, should get location, not locale
                                   LogUtils.i(TAG, "Email Address: " + emailAddress.get());
                                   analyticsManager.signingUpWithFacebook();
                               }
                               location = object.optString("locale");

                           });
                           Bundle parameters = new Bundle();
                           if (facebookToken != null) {
                               parameters.putString("fields", "email,locale");
                           }
                           request.setParameters(parameters);
                           request.executeAsync();

                           if (Profile.getCurrentProfile() != null) {
                               fullname = Profile.getCurrentProfile().getName();
                               avatar = Profile.getCurrentProfile().getProfilePictureUri(512, 512).toString();
                           }
                       });
        });
    };

    public void loginGoogleCompelted(GoogleSignInResult googleResult) {

        if (googleResult != null && googleResult.isSuccess() && googleResult.getSignInAccount() != null && googleResult.getSignInAccount().getIdToken() != null) {
            acct = googleResult.getSignInAccount();
            LogUtils.i(TAG, "GOOGLE LOGIN SUCCESS!!!! - " + acct.getDisplayName());
        }
        else {
            LogUtils.e(TAG, "GOOGLE FAIL - " + googleResult.getStatus());
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_google_login);
            setSuccessfullyLinkedToGoogle(false);
            return;
        }

        String authCode = acct.getServerAuthCode();
        String idToken = acct.getIdToken();
        LogUtils.i(TAG, "google server auth code - " + authCode);
        LogUtils.i(TAG, "google id token  - " + idToken);
        LogUtils.i(TAG, "google email  - " + acct.getEmail());

        googleIdToken = acct.getIdToken();

        setSuccessfullyLinkedToGoogle(true);

        authManager.checkSocial("google", null, null, idToken)
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribeOn(Schedulers.io())
                   .onErrorReturn(throwable -> {
                       if (throwable instanceof HttpException) {
                           setSuccessfullyLinkedToGoogle(false);
                           if (((HttpException) throwable).response().code() == 412) {
                               try {
                                   String body = ((HttpException) throwable).response().errorBody().string();
                                   NetworkSuccessResult result = new Gson().fromJson(body, NetworkSuccessResult.class);
                                   googleIdToken = null;
                                   SnackbarUtil.dismissableSnackbar(getRoot(), result.getError().getMsg());
                               }
                               catch (IOException e) {
                                   //Delete fb token
                                   googleIdToken = null;
                                   setSuccessfullyLinkedToGoogle(false);
                                   SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_google_already_linked);
                               }
                           }
                           else {
                               googleIdToken = null;
                               setSuccessfullyLinkedToGoogle(false);
                               SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_google_connect);
                           }
                       }
                       else {
                           googleIdToken = null;
                           setSuccessfullyLinkedToGoogle(false);
                           SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_google_connect);
                       }
                       setSuccessfullyLinkedToGoogle(false);
                       return null;
                   })
                   .subscribe(networkAuthResponse -> {
                       if (networkAuthResponse != null) {
                           setSuccessfullyLinkedToGoogle(true);
                       }

                       if (idToken != null) {
                           emailAddress.set(acct.getEmail());
                           analyticsManager.signingUpWithGoogle();
                       }
                       //todo the rest
                       fullname = acct.getDisplayName();
                       if (acct.getPhotoUrl() != null) {
                           avatar = acct.getPhotoUrl().toString();
                       }
                   });
    }

    public Action1<View> signupGoogle = view -> {
        //Check if we already connected Google
        if (successfullyLinkedToGoogle) {
            //unlink from google
            setSuccessfullyLinkedToGoogle(false);
            googleIdToken = null;
            emailAddress.set(null);
            fullname = null;
            avatar = null;
        }
        else {
            //being google link
            getActivity().beginGoogleLogin();
        }
    };

    public Action1<Integer> radiusChanged = progress -> {
        notificationRadiusMiles = getRadiusFromProgress(progress);
//        setNotificationRadiusLabel(String.format(Locale.getDefault(), "%.2f mi.", notificationRadiusMiles)); //original code
        setNotificationRadiusLabel(String.format(Locale.getDefault(), "%d mi.", (int) notificationRadiusMiles));
        getActivity().setNotificationRadius(notificationRadiusMiles);
    };

    private double getRadiusFromProgress(int progress) {
        double radius = 0;
        radius = 49 * Math.pow((double) progress / 1000.0, 2.45) + 1;
        return radius;
    }

    private double getRadiusFromProgressOriginal(int progress) {
        double radius = 0;

        if (progress > 56) {
            int amt = progress - 56;
            radius += amt;
            progress -= amt;
        }
        if (progress > 26) {
            int amt = progress - 26;
            radius += (double) amt * 0.5;
            progress -= amt;
        }
        if (progress > 10) {
            int amt = progress - 10;
            radius += (double) amt * 0.25;
            progress -= amt;
        }

        radius += progress * 0.1;

        return radius;
    }

}

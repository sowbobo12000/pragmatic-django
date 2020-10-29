package com.fresconews.fresco.framework.persistence.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.framework.network.requests.NetworkAuthRequest;
import com.fresconews.fresco.framework.network.requests.NetworkInstallation;
import com.fresconews.fresco.framework.network.requests.NetworkLoginRequest;
import com.fresconews.fresco.framework.network.requests.NetworkSignupRequest;
import com.fresconews.fresco.framework.network.responses.NetworkAccessToken;
import com.fresconews.fresco.framework.network.responses.NetworkAuthResponse;
import com.fresconews.fresco.framework.network.responses.NetworkAuthTokenResponse;
import com.fresconews.fresco.framework.network.responses.NetworkTerms;
import com.fresconews.fresco.framework.network.responses.NetworkUser;
import com.fresconews.fresco.framework.network.services.AuthService;
import com.fresconews.fresco.framework.persistence.models.Comment;
import com.fresconews.fresco.framework.persistence.models.Session;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.twitter.sdk.android.core.TwitterAuthToken;

import java.io.IOException;

import rx.Observable;

public class AuthManager {
    private static final String TAG = AuthManager.class.getSimpleName();

    private static final String DEVICE_TOKEN_PREF = "DEVICE_TOKEN_PREF";
    private static final String SAVED_DEVICE_TOKEN_KEY = "SAVED_DEVICE_TOKEN";

    private SharedPreferences sharedpreferences;

    private AuthService authService;
    private SessionManager sessionManager;
    private UserManager userManager;
    private FCMManager fcmManager;
    private NetworkTerms networkTerms;

    public AuthManager(AuthService authService, SessionManager sessionManager, UserManager userManager, FCMManager fcmManager) {
        this.authService = authService;
        this.sessionManager = sessionManager;
        this.userManager = userManager;
        this.fcmManager = fcmManager;
        this.sharedpreferences = Fresco2.getContext().getSharedPreferences(DEVICE_TOKEN_PREF, Context.MODE_PRIVATE);
    }

    public Observable<User> updateInstallation(boolean sendAnyway) {

        return fcmManager.getDeviceToken()
                         .map(deviceToken -> {

                             NetworkLoginRequest request = null;
                             String savedToken = getSavedToken(deviceToken);
                             if ((savedToken == null || deviceToken.equals(savedToken)) && !sendAnyway) {
                                 LogUtils.i(TAG, "Saved Token was the same or we failed to get the saved token");
                             }
                             else if (!deviceToken.equals(savedToken) || sendAnyway) {
                                 LogUtils.i(TAG, "Saved token was different");
                                 request = new NetworkLoginRequest();
                                 request.setUsername(null);
                                 request.setPassword(null);
                                 request.setInstallation(new NetworkInstallation(savedToken, deviceToken, fcmManager.getDeviceId()));
                             }
                             return request;
                         })
                         .flatMap(authService::updateInstallation)
                         .flatMap(this::handleInstallationUpdate)
                         .onErrorReturn(throwable -> {
                             LogUtils.e(TAG, "Login Error ", throwable);
                             return null;
                         });
    }

    public Observable<User> signInNewAuth(String username, String password) {
        NetworkAuthRequest request = new NetworkAuthRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setGrantType("password");
        request.setScope("write");

        return authService.signInNewAuth(request)
                          .map(networkAuthTokenResponse -> {
                              sessionManager.setAttemptingToGenerateClient(false);
                              Session session = sessionManager.getOrCreateSession();
                              if (networkAuthTokenResponse == null) {
                                  LogUtils.i("<-- did not get user token", "oh no!!!");
                                  return null;
                              }

                              LogUtils.i("<-- got user token", "success!!!");
                              NetworkAuthResponse accessToken = networkAuthTokenResponse.getNetworkAuthResponse();
                              session.setUserToken(accessToken.getToken());
                              session.setUserRefreshToken(accessToken.getRefreshToken());
                              sessionManager.setCurrentSession(session); //to save new tokens
                              updateInstallation(true)
                                      .onErrorReturn(throwable -> null).subscribe();
                              return networkAuthTokenResponse;
                          })
                          .flatMap(this::generateTerms)
                          .flatMap(this::continueAuthResponse)
                          .onErrorReturn(throwable -> {
                              return null;
                          });
    }

    public Observable<User> register(NetworkSignupRequest signupRequest) {
        String username = signupRequest.getUsername();
        String password = signupRequest.getPassword();
        return authService.userCreate(signupRequest)
                          .map(user -> {
                              if (user == null) {
                                  return null;
                              }
                              NetworkAuthRequest request = new NetworkAuthRequest();
                              request.setUsername(username);
                              request.setPassword(password);
                              request.setGrantType("password");
                              request.setScope("write");
                              return request;
                          })
                          .flatMap(authService::signInNewAuth)
                          .map(networkAuthTokenResponse -> {
                              sessionManager.setAttemptingToGenerateClient(false);
                              Session session = sessionManager.getOrCreateSession();
                              if (networkAuthTokenResponse == null) {
                                  LogUtils.i("<-- did not get user token", "oh no!!!");
                                  return null;
                              }

                              LogUtils.i("<-- got user token", "success!!!");
                              NetworkAuthResponse accessToken = networkAuthTokenResponse.getNetworkAuthResponse();
                              if (accessToken != null) {
                                  session.setUserToken(accessToken.getToken());
                                  session.setUserRefreshToken(accessToken.getRefreshToken());
                                  sessionManager.setCurrentSession(session); //to save new tokens
                                  updateInstallation(true)
                                          .onErrorReturn(throwable -> null).subscribe();
                              }
                              return networkAuthTokenResponse;
                          })
                          .flatMap(this::generateTerms)
                          .flatMap(this::continueAuthResponse)
                          .onErrorReturn(throwable -> {
                              return null;
                          });
    }

    private Observable<NetworkTerms> generateTerms(NetworkAuthTokenResponse authResponse) {
        if (authResponse == null || authResponse.getNetworkAuthResponse() == null) {
            return null;
        }
        return authService.terms().onErrorReturn(throwable -> {
            return null;
        });
    }

    private Observable<User> continueAuthResponse(NetworkTerms networkTerms) {
        boolean isValidPassword = networkTerms != null;

        setNetworkTerms(networkTerms);

        return userManager.me()
                          .map(user -> {
                              Session session = sessionManager.getCurrentSession();
                              if (session != null) {
                                  session.setUserId(user.getId());
                                  session.setUsername(user.getUsername());
                                  sessionManager.setCurrentSession(session);
                              }
                              else {
                                  LogUtils.i(TAG, "No session while trying to sign in, ignoring");
                              }
                              if (user != null) {
                                  user.setValidPassword(isValidPassword);
                              }
                              return user;
                          })
                          .onErrorReturn(throwable -> null);
    }

    private Observable<User> handleAuthResponse(NetworkAuthResponse authResponse) {
        String token = authResponse.getToken();
        boolean isValidPassword = authResponse.isValidPassword();

        Session tempSession = sessionManager.getOrCreateSession();
        tempSession.setToken(token);
        tempSession.setUserToken(token);
        tempSession.setUserRefreshToken(authResponse.getRefreshToken());

        updateInstallation(true)
                .onErrorReturn(throwable -> null)
                .subscribe();

        sessionManager.setCurrentSession(tempSession);
        setNetworkTerms(authResponse.getTerms());

        return userManager.me()
                          .map(user -> {
                              Session session = sessionManager.getCurrentSession();
                              if (session != null) {
                                  session.setUserId(user.getId());
                                  session.setUsername(user.getUsername());
                                  sessionManager.setCurrentSession(session);
                              }
                              else {
                                  LogUtils.i(TAG, "No session while trying to sign in, ignoring");
                              }
                              if (user != null) {
                                  user.setValidPassword(isValidPassword);
                              }
                              return user;
                          })
                          .onErrorReturn(throwable -> null);
    }

    public Observable<User> signInTwitter(TwitterAuthToken authToken) {
        String twitterToken = authToken.token;
        String twitterSecret = authToken.secret;

        return fcmManager.getDeviceToken()
                         .map(deviceToken -> {
                             NetworkLoginRequest request = new NetworkLoginRequest();
                             request.setToken(twitterToken);
                             request.setSecret(twitterSecret);
                             request.setPlatform(NetworkLoginRequest.TWITTER);
                             String savedToken = getSavedToken(deviceToken);
                             if (savedToken == null || deviceToken.equals(savedToken)) {
                                 request.setInstallation(new NetworkInstallation(deviceToken, fcmManager.getDeviceId()));
                             }
                             else if (!deviceToken.equals(savedToken)) {
                                 request.setInstallation(new NetworkInstallation(savedToken, deviceToken, fcmManager.getDeviceId()));
                             }
                             return request;
                         })
                         .flatMap(authService::signInSocial)
                         .flatMap(this::handleAuthResponse)
                         .onErrorReturn(throwable -> null);
    }

    public Observable<User> signInFacebook(AccessToken accessToken) {
        String token = accessToken.getToken();

        return fcmManager.getDeviceToken()
                         .map(deviceToken -> {
                             NetworkLoginRequest request = new NetworkLoginRequest();
                             request.setToken(token);
                             request.setPlatform(NetworkLoginRequest.FACEBOOK);
                             String savedToken = getSavedToken(deviceToken);
                             if (savedToken == null || deviceToken.equals(savedToken)) {
                                 request.setInstallation(new NetworkInstallation(deviceToken, fcmManager.getDeviceId()));
                             }
                             else if (!deviceToken.equals(savedToken)) {
                                 request.setInstallation(new NetworkInstallation(savedToken, deviceToken, fcmManager.getDeviceId()));
                             }
                             return request;
                         })
                         .flatMap(authService::signInSocial)
                         .flatMap(this::handleAuthResponse)
                         .onErrorReturn(throwable -> null);
    }

    public Observable<User> signInGoogle(String token) {

        return fcmManager.getDeviceToken()
                         .map(deviceToken -> {
                             NetworkLoginRequest request = new NetworkLoginRequest();
                             request.setJwt(token);
                             request.setPlatform(NetworkLoginRequest.GOOGLE);
                             String savedToken = getSavedToken(deviceToken);
                             if (savedToken == null || deviceToken.equals(savedToken)) {
                                 request.setInstallation(new NetworkInstallation(deviceToken, fcmManager.getDeviceId()));
                             }
                             else if (!deviceToken.equals(savedToken)) {
                                 request.setInstallation(new NetworkInstallation(savedToken, deviceToken, fcmManager.getDeviceId()));
                             }
                             return request;
                         })
                         .flatMap(authService::signInSocial)
                         .flatMap(this::handleAuthResponse)
                         .onErrorReturn(throwable -> null);
    }

    private String getSavedToken(String generatedToken) {
        String savedToken = null;

        if (sharedpreferences == null) {
            return null;
        }

        savedToken = sharedpreferences.getString(SAVED_DEVICE_TOKEN_KEY, null);
        if (savedToken == null) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(SAVED_DEVICE_TOKEN_KEY, generatedToken);
            editor.apply();
            return generatedToken;
        }
        if (!savedToken.equals(generatedToken)) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(SAVED_DEVICE_TOKEN_KEY, generatedToken);
            editor.apply();
            return savedToken;
        }

        return null;
    }

    public Observable<Void> checkSocial(String platform, String token, String secret, String authCode) {
        return authService.checkSocial(platform, token, secret, authCode);
    }

    private Observable<User> handleInstallationUpdate(NetworkUser networkUser) {
        return userManager.me().onErrorReturn(throwable -> null);

    }

    public void logout() {
        LoginManager.getInstance().logOut(); //fb logout
        Fresco2.stopLocationService();
        sessionManager.logoutCurrentSession();
        authService.signOut() //relies on the temp deleteToken in session.
                   .onErrorReturn(throwable -> {
                       LogUtils.e(TAG, "Error signing out", throwable);
                       return null;
                   })
                   .subscribe(networkAuthResponse -> {
                       authService.deleteUserToken()
                                  .onErrorReturn(throwable -> null)
                                  .subscribe(); //deletes the temp deleteToken in session.
                   });
    }

    public NetworkTerms getNetworkTerms() {
        return networkTerms;
    }

    public void setNetworkTerms(NetworkTerms networkTerms) {
        this.networkTerms = networkTerms;
    }

}

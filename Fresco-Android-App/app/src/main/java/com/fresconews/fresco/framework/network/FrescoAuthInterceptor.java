package com.fresconews.fresco.framework.network;

import android.databinding.Observable;
import android.support.design.widget.TabLayout;

import com.fresconews.fresco.BuildConfig;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.framework.network.requests.NetworkAuthRequest;
import com.fresconews.fresco.framework.network.responses.NetworkAccessToken;
import com.fresconews.fresco.framework.network.responses.NetworkAuthResponse;
import com.fresconews.fresco.framework.network.responses.NetworkAuthTokenResponse;
import com.fresconews.fresco.framework.network.services.AuthService;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.models.Session;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.NetworkUtils;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;

/**
 * Created by ryan on 6/13/2016.
 */
public class FrescoAuthInterceptor implements Interceptor {

    private SessionManager sessionManager;
    private boolean checkedBearerMeVersion = false;
    private boolean currentlyCheckingBearerMe = false;
    //booleans above required for when user updates the app. Read more below in updateBearerIfNecessary().

    @Inject
    AuthService authService;

    public FrescoAuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response;

        String url = request.url().toString();
        LogUtils.d("OkHttp", url);
        ((Fresco2) Fresco2.getContext().getApplicationContext()).getFrescoComponent().inject(this);
        Session currentSession = sessionManager.getCurrentSession();

        //Stripe
        if (url.contains("v1/tokens") || url.contains("v1/files")) {
            Request stripeRequest = request.newBuilder()
                                           .addHeader("Authorization", "Bearer " + EndpointHelper.currentEndpoint.stripeKey)
                                           .build();
            response = chain.proceed(stripeRequest);
            return response;
        }

        if (!checkedBearerMeVersion && currentSession != null && !currentlyCheckingBearerMe) {
            currentlyCheckingBearerMe = true;
            updateBearerIfNecessary(); //todo is it possible this is called after a refresch of current session? :oh no:
        }

        if (url.contains("auth/signout") || (url.contains("auth/token") && request.method().equals("DELETE"))) {
            //both auth/signout and auth/token DELETE rely on a deleteBearer which is temporarily saved in SessionManager
            String deleteBearer = sessionManager.getUserDeleteToken();
            Request bearerRequest = request.newBuilder()
                                           .addHeader("Authorization", "Bearer " + deleteBearer)
                                           .build();
            response = chain.proceed(bearerRequest); //todo timeout

            if (request.method().equals("DELETE")) { //delete the temporary userDeleteToken, called after auth/signout
                sessionManager.setUserDeleteToken(null);
                sessionManager.setCurrentSession(currentSession);
            }
            return response;
        }
        else if (shouldSendBasic(request)) {
            LogUtils.i("<-- FRESCOAUTH", "SENDING BASIC OVERRIDE");

            Request basicRequest = request.newBuilder()
                                          .addHeader("Authorization", "Basic " + EndpointHelper.currentEndpoint.frescoClientId)
                                          .build();

            response = chain.proceed(basicRequest);
            return response;
        }
        else if (shouldSendClientToken(request) && currentSession != null) {
            LogUtils.i("<-- FRESCOAUTH", "SENDING CLIENT TOKEN");
            Request clientRequest = request.newBuilder()
                                           .addHeader("Authorization", "Bearer " + currentSession.getClientToken())
                                           .build();

            response = chain.proceed(clientRequest);
            return response;
        }
        else if (shouldSendUserToken(request) && currentSession != null) {
            LogUtils.i("<-- FRESCOAUTH", "SENDING USER BEARER " + request.method());

            Request bearerRequest = request.newBuilder()
                                           .addHeader("Authorization", "Bearer " + currentSession.getUserToken())
                                           .build();
            response = chain.proceed(bearerRequest); //todo timeout
            return response;
        }

        //Otherwise, send the basic auth
        LogUtils.i("<-- FRESCOAUTH", "SENDING BASIC");
        Request basicRequest = request.newBuilder()
                                      .addHeader("Authorization", "Basic " + EndpointHelper.currentEndpoint.frescoClientId)
                                      .build();

        response = chain.proceed(basicRequest);
        createClientID(request); //create a client ID

        return response;
    }

    private void createClientID(Request request) {
        String url = request.url().toString();

        //currently attempting to generate the client auth token
        if (!url.contains("auth/token") && !sessionManager.isAttemptingToGenerateClient()) {
            sessionManager.setAttemptingToGenerateClient(true);
            generateClientToken()
                    .onErrorReturn(throwable -> null)
                    .subscribe(networkAuthTokenResponse -> {
                        sessionManager.setAttemptingToGenerateClient(false);
                        Session session = sessionManager.getOrCreateSession();
                        if (networkAuthTokenResponse == null) {
                            LogUtils.i("<-- did not get client token", "oh no!!!");
                            return;
                        }

                        LogUtils.i("<-- got client token", "success!!!");
                        NetworkAuthResponse accessToken = networkAuthTokenResponse.getNetworkAuthResponse();
                        session.setClientToken(accessToken.getToken());
                        session.setClientRefreshToken(accessToken.getRefreshToken());
                        sessionManager.setCurrentSession(session); //to save new tokens
                    });
        }
    }

    private boolean shouldSendClientToken(Request request) {
        String url = request.url().toString();
        return url.contains("fresconews") && !sessionManager.isLoggedIn() && sessionManager.hasClientToken();
    }

    private boolean shouldSendUserToken(Request request) {
        String url = request.url().toString();
        return url.contains("fresconews") && sessionManager.isLoggedIn();
    }

    private boolean shouldSendBasic(Request request) {
        String url = request.url().toString();
        boolean sendBasic = url.contains("auth/signin") || url.contains("auth/token");
        sendBasic = sendBasic && !"DELETE".equals(request.method());
        return sendBasic;
    }

    public rx.Observable<NetworkAuthTokenResponse> generateClientToken() {
        NetworkAuthRequest requestBody = new NetworkAuthRequest();
        requestBody.setGrantType("client_credentials");
        requestBody.setScope("write");
        return authService.generateClientToken(requestBody);
    }

    public void updateBearerIfNecessary() {
        Session currentSession = sessionManager.getCurrentSession();

        if (currentSession == null) { //this shouldn't happen anyway, checked before called
            LogUtils.i("frescosession", "failure to load the current session, will use client bearer");
            return;
        }

        //Find out if the app version is different
        if (BuildConfig.VERSION_CODE == currentSession.getPastVersionCode()) {
            /* We discussed checking against the API version but determined that it's unnecessary and dangerous to keep
            active tabs on api differences in coordination with android app versions. Sometimes a hotfix or major
            production level change should be made AHEAD of a push from android, e.g. oauth differences in 3.1.2
            The bearer will be refreshed using this flow every time the Android App is updated. */
            currentlyCheckingBearerMe = false;
            checkedBearerMeVersion = true;
            return;
        }

        LogUtils.i("frescosession", "updating bearer!!!");

        //delete the client token so i generate it again.
        currentSession.setClientToken(null);
        currentSession.setClientRefreshToken(null);
        sessionManager.setCurrentSession(currentSession);

        //If so, check to see if we currently have a user bearer. If so, gotta update that ish.
        if (currentSession.getUserToken() == null && currentSession.getUserRefreshToken() == null) {
            LogUtils.i("frescosession", "user token was null!!! gasp!");
            checkedBearerMeVersion = true;
            currentlyCheckingBearerMe = false;
            return;
        }

        try {
            NetworkAuthRequest requestBody = new NetworkAuthRequest();
            requestBody.setOldBearerToken(currentSession.getUserToken());
            requestBody.setRefreshToken(currentSession.getUserRefreshToken());
            Call<NetworkAuthResponse> call = authService.updateToken(requestBody);
            NetworkAuthResponse accessToken = call.execute().body();
            LogUtils.i("frescosession", "attached tokens and past version code, called to update token!!");

            if (accessToken != null) {
                currentSession.setUserToken(accessToken.getToken());
                currentSession.setUserRefreshToken(accessToken.getRefreshToken());
                currentSession.setPastVersionCode(BuildConfig.VERSION_CODE); //If successful, save the new app version in sharedpreferences
                sessionManager.setCurrentSession(currentSession); //to save new tokens
                checkedBearerMeVersion = true;
                currentlyCheckingBearerMe = false;
            }
        }
        catch (IOException e) {
            if (e.getMessage() != null) {
                LogUtils.i("frescosession", "tried to update app version and fucked up!!!");
                LogUtils.i("<-- AuthInterceptor", "REFRESHING USER ERROR -- " + e.getMessage());
            }
        }
        currentlyCheckingBearerMe = false;
    }
}

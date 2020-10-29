package com.fresconews.fresco.framework.network;

import com.android.annotations.Nullable;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.framework.network.requests.NetworkAuthRequest;
import com.fresconews.fresco.framework.network.responses.NetworkAccessToken;
import com.fresconews.fresco.framework.network.responses.NetworkAuthResponse;
import com.fresconews.fresco.framework.network.responses.NetworkAuthTokenResponse;
import com.fresconews.fresco.framework.network.services.AuthService;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.models.Session;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.StringUtils;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

/**
 * Created by Blaze on 1/19/2017.
 */

public class FrescoTokenAuthenticator implements Authenticator {

    @Inject
    AuthService authService;

    private SessionManager sessionManager;

    public FrescoTokenAuthenticator(SessionManager sessionManager) {
        this.sessionManager = sessionManager;

    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        LogUtils.i("<-- AuthInterceptor", "response -- " + Integer.toString(response.code()));
        ((Fresco2) Fresco2.getContext().getApplicationContext()).getFrescoComponent().inject(this);
        Session currentSession = sessionManager.getOrCreateSession();

        //figure out if user token or client token needs to be refreshed
        boolean refreshClient = currentSession.getClientToken() != null && currentSession.getClientRefreshToken() != null
                && StringUtils.toNullIfEmpty(currentSession.getUserToken()) == null;

        boolean refreshUser = currentSession.getUserToken() != null && currentSession.getUserRefreshToken() != null;

        //CLIENT TOKEN
        if (refreshClient) {
            //destroy the current token to trigger client token refresh
            currentSession.setClientToken(null);
            sessionManager.setCurrentSession(currentSession);
            refreshClientToken(currentSession.getClientRefreshToken());

            LogUtils.i("<-- AuthInterceptor", "REFRESHING CLIENT ");

            if (StringUtils.toNullIfEmpty(currentSession.getClientToken()) != null) {
                Request clientRequest = response.request().newBuilder()
                                                .addHeader("Authorization", "Bearer " + currentSession.getClientToken())
                                                .build();

                return clientRequest;
            }
        }

        //USER AUTH TOKEN
        if (refreshUser) {
            //destroy the current token to trigger user token refresh
            currentSession.setUserToken(null);
            //todo is this asymmetric? that would be bad
            NetworkAuthTokenResponse refreshTokenResponse = refreshUserToken(currentSession.getUserRefreshToken());
            LogUtils.i("<-- AuthInterceptor", "REFRESHING USER -- " + Integer.toString(response.code()));
            Request bearerRequest = null;
            bearerRequest = response.request().newBuilder()
                                    .addHeader("Authorization", "Bearer " + currentSession.getUserToken())
                                    .build();

            return bearerRequest;
        }
        return null;
    }

    @Nullable
    public NetworkAuthTokenResponse refreshClientToken(String refreshToken) {
        Session currentSession = sessionManager.getCurrentSession();
        if (currentSession == null) {
            return null;
        }
        NetworkAuthTokenResponse refreshAuthResponse = null;
        try {
            refreshAuthResponse = refreshToken(refreshToken).execute().body();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if (refreshAuthResponse != null) {
            NetworkAuthResponse accessToken = refreshAuthResponse.getNetworkAuthResponse();
            currentSession.setClientToken(accessToken.getToken());
            currentSession.setClientRefreshToken(accessToken.getRefreshToken());
            sessionManager.setCurrentSession(currentSession); //to save new tokens
        }
        return refreshAuthResponse;
    }

    public NetworkAuthTokenResponse refreshUserToken(String refreshToken) {
        Session currentSession = sessionManager.getOrCreateSession();

        NetworkAuthTokenResponse refreshAuthResponse = null;
        try {
            NetworkAuthRequest requestBody = new NetworkAuthRequest();
            requestBody.setGrantType("refresh_token");
            requestBody.setScope("write");
            requestBody.setRefreshToken(refreshToken);
            LogUtils.i("<-- AuthInterceptor", "REFRESHING USER WITH TOKEN-- " + refreshToken);
            if (authService == null) {
                LogUtils.i("<-- AuthInterceptor", "how ");

            }
            Call<NetworkAuthTokenResponse> call = authService.refreshToken(requestBody);

//            refreshAuthResponse = refreshToken(refreshToken).execute().body();
            refreshAuthResponse = call.execute().body();
        }
        catch (IOException e) {
            e.printStackTrace();
            LogUtils.i("<-- AuthInterceptor", "REFRESHING USER ERROR -- " + e.getMessage());
        }
        if (refreshAuthResponse != null && refreshAuthResponse.getNetworkAuthResponse() != null) {
            NetworkAuthResponse accessToken = refreshAuthResponse.getNetworkAuthResponse();
            if (accessToken != null) {
                currentSession.setUserToken(accessToken.getToken());
                currentSession.setUserRefreshToken(accessToken.getRefreshToken());
                sessionManager.setCurrentSession(currentSession); //to save new tokens
            }
        }

        return refreshAuthResponse;
    }

    public Call<NetworkAuthTokenResponse> refreshToken(String refreshToken) {
        NetworkAuthRequest requestBody = new NetworkAuthRequest();
        requestBody.setGrantType("refresh_token");
        requestBody.setScope("write");
        requestBody.setRefreshToken(refreshToken);
        return authService.refreshToken(requestBody);
    }
}

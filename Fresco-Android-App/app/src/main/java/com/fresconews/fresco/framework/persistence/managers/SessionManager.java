package com.fresconews.fresco.framework.persistence.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.databinding.Observable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.fresconews.fresco.BuildConfig;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.framework.network.requests.NetworkAuthRequest;
import com.fresconews.fresco.framework.network.responses.NetworkAccessToken;
import com.fresconews.fresco.framework.network.responses.NetworkAuthResponse;
import com.fresconews.fresco.framework.network.responses.NetworkAuthTokenResponse;
import com.fresconews.fresco.framework.network.services.AuthService;
import com.fresconews.fresco.framework.persistence.models.Session;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.StringUtils;

import javax.inject.Inject;

/**
 * Created by ryan on 6/13/2016.
 */
public class SessionManager {
    private static final String SESSION_PREFERENCES = "FRESCO_SESSION";

    private static final String SESSION_USERNAME = "FRESCO_SESSION_USERNAME";
    private static final String SESSION_USERID = "FRESCO_SESSION_USERID";
    private static final String FRESCO_APP_VERSION = "FRESCO_APP_VERSION";

    private static final String SESSION_TOKEN = "FRESCO_SESSION_TOKEN"; //todo remove in 3.1.3
    //Current session used to have just getToken(), so we have to check for that as well first. After this
    // version we can delete the getToken();
    private static final String CLIENT_TOKEN = "FRESCO_CLIENT_TOKEN";
    private static final String CLIENT_REFRESH_TOKEN = "FRESCO_CLIENT_REFRESH_TOKEN";
    private static final String USER_TOKEN = "FRESCO_USER_TOKEN";
    private static final String USER_REFRESH_TOKEN = "FRESCO_USER_REFRESH_TOKEN";

    private Context context;

    private Session currentSession;
    private String userDeleteToken;
    private boolean attemptingToGenerateClient;

    public SessionManager(Context context) {
        this.context = context;
    }

    public void setCurrentSession(Session session) {
        currentSession = session;
        saveCurrentSession();
    }

    private void saveCurrentSession() {

        LogUtils.i("frescosession", "saving current session");

        SharedPreferences preferences = context.getSharedPreferences(SESSION_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(SESSION_USERNAME, currentSession.getUsername());
        editor.putString(SESSION_USERID, currentSession.getUserId());
        editor.putInt(FRESCO_APP_VERSION, BuildConfig.VERSION_CODE);

        editor.putString(CLIENT_TOKEN, currentSession.getClientToken());
        editor.putString(CLIENT_REFRESH_TOKEN, currentSession.getClientRefreshToken());
        editor.putString(USER_TOKEN, currentSession.getUserToken());
        editor.putString(USER_REFRESH_TOKEN, currentSession.getUserRefreshToken());

        editor.apply();
    }

    @Nullable
    private Session loadCurrentSession() {
        SharedPreferences preferences = context.getSharedPreferences(SESSION_PREFERENCES, Context.MODE_PRIVATE);

        String username = preferences.getString(SESSION_USERNAME, "");
        String userId = preferences.getString(SESSION_USERID, "");
        int pastVersionCode = preferences.getInt(FRESCO_APP_VERSION, -1);

        String token = preferences.getString(SESSION_TOKEN, "EMPTY"); //todo remove in 3.1.3
        String userToken = preferences.getString(USER_TOKEN, "EMPTY");
        String userRefreshToken = preferences.getString(USER_REFRESH_TOKEN, "EMPTY-USER-REFRESH");
        String clientToken = preferences.getString(CLIENT_TOKEN, "EMPTY-CLIENT");
        String clientRefreshToken = preferences.getString(CLIENT_REFRESH_TOKEN, "EMPTY-CLIENT-REFRESH");

        if (userToken.equals("EMPTY")) { //todo remove in 3.1.3
            userToken = token;
        }

        if (userToken.equals("EMPTY")) {
            LogUtils.i("frescosession", "couldn't load session");
            return null;
        }

        Session session = new Session();
        session.setUsername(username);
        session.setUserId(userId);
        session.setPastVersionCode(pastVersionCode);

        session.setToken(token); //todo remove in 3.1.3
        session.setUserToken(userToken);
        session.setUserRefreshToken(userRefreshToken);
        session.setClientToken(clientToken);
        session.setClientRefreshToken(clientRefreshToken);

        return session;
    }

    public Session getOrCreateSession() {
        if (currentSession != null) {
            return currentSession;
        }
        currentSession = loadCurrentSession();
        if (currentSession == null) {
            LogUtils.i("frescosession", "creating new session even though i shouldn't have to");

            currentSession = new Session();
        }
        LogUtils.i("frescosession", "returning a loaded or new session");

        return currentSession;
    }

    private void deleteCurrentSession() {
        LogUtils.i("frescosession", "deleting session");

        SharedPreferences preferences = context.getSharedPreferences(SESSION_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove(SESSION_USERNAME);
        editor.remove(SESSION_USERID);

        editor.remove(SESSION_TOKEN); //todo remove in 3.1.3
        editor.remove(USER_TOKEN);
        editor.remove(USER_REFRESH_TOKEN);
        editor.remove(CLIENT_TOKEN);
        editor.remove(CLIENT_REFRESH_TOKEN);
        editor.apply();

        if (currentSession == null) {
            return;
        }
        userDeleteToken = currentSession.getUserToken();

        currentSession.setToken(null); //todo remove in 3.1.3
        currentSession.setUserToken(null);
        currentSession.setUserRefreshToken(null);
        currentSession.setClientToken(null);
        currentSession.setClientRefreshToken(null);
        currentSession = null;
    }

    @Nullable
    public Session getCurrentSession() {
        if (currentSession == null) {
            currentSession = loadCurrentSession();
        }
        return currentSession;
    }

    public void logoutCurrentSession() {
        deleteCurrentSession(); //delete the user session

//        reinstating the client token or using  new one using the basic auth will be triggered by the next network call
//        the logic (of determining, and refreshing if necessary) is in FrescoTokenAuthenticator
    }

    public boolean isLoggedIn() {
        return getCurrentSession() != null && StringUtils.toNullIfEmpty(getCurrentSession().getUserToken()) != null;
    }

    public boolean hasClientToken() {
        return getCurrentSession() != null && StringUtils.toNullIfEmpty(getCurrentSession().getClientToken()) != null;
    }

    public void setAttemptingToGenerateClient(boolean isAttempting) {
        attemptingToGenerateClient = isAttempting;
    }

    public boolean isAttemptingToGenerateClient() {
        return attemptingToGenerateClient;
    }

    public String getUserDeleteToken() {
        return userDeleteToken;
    }

    public void setUserDeleteToken(String userDeleteToken) {
        LogUtils.i("frescosession", "deleteing user delete token!!");
        this.userDeleteToken = userDeleteToken;
    }
}

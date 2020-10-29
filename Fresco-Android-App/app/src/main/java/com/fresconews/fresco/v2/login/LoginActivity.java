package com.fresconews.fresco.v2.login;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.network.EndpointHelper;
import com.fresconews.fresco.framework.persistence.managers.AuthManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.v2.home.HomeActivity;
import com.fresconews.fresco.v2.mediabrowser.MediaItemViewModel;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.util.ArrayList;

import javax.inject.Inject;

import io.smooch.ui.ConversationActivity;
import rx.Observable;

public class LoginActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = LoginActivity.class.getSimpleName();
    public static final String SUBMISSIONS = "submissions";
    private static final int KILL_ME = 666;
    private static final int KEEP_ALIVE = 777;
    private static final int RC_SIGN_IN = 9001;

    private TwitterAuthClient mAuthClient;
    private GoogleApiClient googleApiClient;
    private GoogleSignInOptions gso;
    private CallbackManager mFBCallbackManager;

    @Inject
    UserManager userManager;

    @Inject
    AuthManager authManager;

    public static void start(Context context) {
        Intent starter = new Intent(context, LoginActivity.class);
        context.startActivity(starter);
    }

    public static void start(Context context, ArrayList<MediaItemViewModel> mediaItems) {
        Intent starter = new Intent(context, LoginActivity.class);
        starter.putParcelableArrayListExtra(SUBMISSIONS, mediaItems);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);

        LoginViewModel viewModel = new LoginViewModel(this);
        ArrayList<MediaItemViewModel> mediaItems = getIntent().getParcelableArrayListExtra(SUBMISSIONS);
        if (mediaItems != null) {
            setViewModel(R.layout.activity_login, new LoginViewModel(this, mediaItems));
        }
        else {
            setViewModel(R.layout.activity_login, viewModel);
        }

        // Configure sign-in to request the user's ID, email address, and basic profile. ID and
        // basic profile are included in DEFAULT_SIGN_IN.
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(EndpointHelper.currentEndpoint.googleOauthServerCredentialId)
                .requestServerAuthCode(EndpointHelper.currentEndpoint.googleOauthServerCredentialId)
                .requestEmail()
                .build();

        /*
        Note: To use enableAutoManage, your activity must extend FragmentActivity or AppCompatActivity
        (a subclass of FragmentActivity), both of which are part of the Android Support Library. You
         can use GoogleApiClient in a Fragment; however, the fragment's parent activity must be a FragmentActivity.
         If you can't extend FragmentActivity, you must manually manage the GoogleApiClient connection lifecycle.
         */

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }

        mFBCallbackManager = CallbackManager.Factory.create();
    }

    public Observable<TwitterSession> getTwitterSession() {
        return Observable.create(subscriber -> {
            getTwitterAuthClient().authorize(this, new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    subscriber.onNext(result.data);
                    subscriber.onCompleted();
                }

                @Override
                public void failure(TwitterException exception) {
                    Log.e(TAG, "Twitter Error", exception);
                    subscriber.onCompleted();
                }
            });
        });
    }

    private TwitterAuthClient getTwitterAuthClient() {
        if (mAuthClient == null) {
            synchronized (LoginActivity.class) {
                if (mAuthClient == null) {
                    mAuthClient = new TwitterAuthClient();
                }
            }
        }
        return mAuthClient;
    }

    public Observable<AccessToken> getFacebookSession() {
        return Observable.create(subscriber -> {
            if (AccessToken.getCurrentAccessToken() != null) {
                subscriber.onNext(AccessToken.getCurrentAccessToken());
                subscriber.onCompleted();
                return;
            }

            LoginManager.getInstance().registerCallback(mFBCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    subscriber.onNext(loginResult.getAccessToken());
                    subscriber.onCompleted();
                }

                @Override
                public void onCancel() {
                    Log.i(TAG, "Facebook login cancel");
                    subscriber.onCompleted();
                }

                @Override
                public void onError(FacebookException error) {
                    Log.e(TAG, "Facebook Error", error);
                    subscriber.onCompleted();
                }
            });
            LoginManager.getInstance().logInWithReadPermissions(this, null);
        });
    }

    public void beginGoogleLogin() {

        // Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        getTwitterAuthClient().onActivityResult(requestCode, resultCode, data);
        mFBCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from
//           GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            LogUtils.i(TAG, "google result code - " + Integer.toString(resultCode));

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            ((LoginViewModel) getViewModel()).loginGoogleCompelted(result);
        }
        else {
            LogUtils.i(TAG, "google different activity result - " + Integer.toString(requestCode));
        }
    }

    public void killMe(User user) {
        Intent databackIntent = new Intent();

        if (isTaskRoot()) {
            HomeActivity.start(this, true);
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setResult(KILL_ME, databackIntent);
            finishAndRemoveTask();
        }
        else {
            setResult(KILL_ME, databackIntent);
            finish();
        }
    }

    public void killMe() {
        Intent databackIntent = new Intent();
        if (isTaskRoot()) {
            //Show suspend user if necessary

            HomeActivity.start(this, true);
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setResult(KILL_ME, databackIntent);
            finishAndRemoveTask();
        }
        else {
            setResult(KILL_ME, databackIntent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent databackIntent = new Intent();
        setResult(KEEP_ALIVE, databackIntent);
        super.onBackPressed();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //todo add snackbar
    }
}

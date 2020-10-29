package com.fresconews.fresco.v2.signup;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

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
import com.fresconews.fresco.framework.persistence.managers.FrescoLocationManager;
import com.fresconews.fresco.framework.rx.RxGoogleMaps;
import com.fresconews.fresco.v2.home.HomeActivity;
import com.fresconews.fresco.v2.mediabrowser.MediaItemViewModel;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;

public class SignupActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = SignupActivity.class.getSimpleName();
    private static final int KILL_ME = 666;
    private static final int RC_SIGN_IN = 9001;
    private static final float METERS_IN_MILE = 1609.34f;
    private static final LatLng DEFAULT_COORD = new LatLng(40.7128, -74.0059);

    public static final String SUBMISSIONS = "submissions";

    @Inject
    FrescoLocationManager frescoLocationManager;

    private TwitterAuthClient twitterAuthClient;
    private CallbackManager callbackManager;
    private GoogleApiClient googleApiClient;
    private GoogleSignInOptions gso;
    private GoogleMap googleMap;
    private Circle circle;
    private double notificationRadius;

    public static void start(Context context, ArrayList<MediaItemViewModel> mediaItems) {
        Intent starter = new Intent(context, SignupActivity.class);
        starter.putParcelableArrayListExtra(SUBMISSIONS, mediaItems);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);

        SignupViewModel viewModel = new SignupViewModel(this);

        setViewModel(R.layout.activity_signup, viewModel);

        callbackManager = CallbackManager.Factory.create();

        TextInputLayout passwordLayout = (TextInputLayout) findViewById(R.id.password_text_input_layout);

        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        RxGoogleMaps.getMap(mapFragment)
                    .map(googleMap -> this.googleMap = googleMap)
                    .compose(new RxPermissions(this).ensure(android.Manifest.permission.ACCESS_FINE_LOCATION))
                    .onErrorReturn(throwable -> null)
                    .subscribe(granted -> {
                        if (granted != null && granted && googleMap != null) {
                            //noinspection MissingPermission
                            googleMap.setMyLocationEnabled(true);
                        }
                        else {
                            SnackbarUtil.dismissableSnackbar(getDataBinding().getRoot(), R.string.permission_location_denied);
                        }
                        initMap();
                    });

        // Configure sign-in to request the user's ID, email address, and basic profile. ID and
        // basic profile are included in DEFAULT_SIGN_IN.
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_ME))
                .requestIdToken(EndpointHelper.currentEndpoint.googleOauthServerCredentialId)
                .requestServerAuthCode(EndpointHelper.currentEndpoint.googleOauthServerCredentialId)
                .requestEmail()
                .build();

        //.requestScopes(new Scope(Scopes.PLUS_LOGIN))
        //.requestScopes(new Scope("https://www.googleapis.com/auth/userinfo.profile"))

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
                    LogUtils.e(TAG, "Twitter Error", exception);
                    SnackbarUtil.dismissableSnackbar(getDataBinding().getRoot(), R.string.error_twitter_connect);
                    subscriber.onCompleted();
                }
            });
        });
    }

    private TwitterAuthClient getTwitterAuthClient() {
        if (twitterAuthClient == null) {
            synchronized (SignupActivity.class) {
                if (twitterAuthClient == null) {
                    twitterAuthClient = new TwitterAuthClient();
                }
            }
        }
        return twitterAuthClient;
    }

    public Observable<AccessToken> getFacebookSession() {
        return Observable.create(subscriber -> {
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    subscriber.onNext(loginResult.getAccessToken());
                    subscriber.onCompleted();
                }

                @Override
                public void onCancel() {
                    LogUtils.w(TAG, "Facebook login cancel");
                    if (AccessToken.getCurrentAccessToken() != null) {
                        subscriber.onNext(AccessToken.getCurrentAccessToken());
                    }
                    subscriber.onCompleted();
                }

                @Override
                public void onError(FacebookException error) {
                    LogUtils.e(TAG, "Facebook Error", error);
                    if (error.getMessage().contains("User logged in as different Facebook user.")) {
                        LoginManager.getInstance().logOut();
                        getFacebookSession();
                    }
                    else {
                        SnackbarUtil.dismissableSnackbar(getDataBinding().getRoot(), error.getMessage());
                    }
                    subscriber.onCompleted();
                }
            });
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
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

        if (requestCode == getTwitterAuthClient().getRequestCode()) {
            getTwitterAuthClient().onActivityResult(requestCode, resultCode, data);
        }
        else if (resultCode == KILL_ME) {
            killMe();
        }
        // Result returned from launching the Intent from
//           GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            LogUtils.i(TAG, "google result code - " + Integer.toString(resultCode));

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            ((SignupViewModel) getViewModel()).loginGoogleCompelted(result);
        }
        else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void setNotificationRadius(double mNotificationRadius) {
        this.notificationRadius = mNotificationRadius;
        updateMapBounds();
    }

    private void updateMapBounds() {
        if (circle == null) {
            return;
        }
        LatLng latLng = circle.getCenter();
        double radiusInMeters = notificationRadius * METERS_IN_MILE;
        double viewRadiusMeters = radiusInMeters;

        if (viewRadiusMeters == 0) {
            viewRadiusMeters = 161; // 1/10th of a mile
        }

        LatLngBounds circleBounds = new LatLngBounds.Builder().include(SphericalUtil.computeOffset(latLng, viewRadiusMeters, 0))
                                                              .include(SphericalUtil.computeOffset(latLng, viewRadiusMeters, 90))
                                                              .include(SphericalUtil.computeOffset(latLng, viewRadiusMeters, 180))
                                                              .include(SphericalUtil.computeOffset(latLng, viewRadiusMeters, 270))
                                                              .build();

        circle.setRadius(radiusInMeters);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(circleBounds, 400, 400, 0));
    }

    private void initMap() {
        if (frescoLocationManager.getReactiveLocationProvider() == null) {
            frescoLocationManager.setReactiveLocationProvider(new ReactiveLocationProvider(this));
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        frescoLocationManager.getReactiveLocationProvider()
                             .getLastKnownLocation()
                             .defaultIfEmpty(null)
                             .onErrorReturn(throwable -> null)
                             .subscribe(location -> {
                                 LatLng coords;
                                 if (location == null) {
                                     coords = DEFAULT_COORD; // Default to NYC
                                     SnackbarUtil.dismissableSnackbar(getDataBinding().getRoot(), R.string.location_not_found);
                                 }
                                 else {
                                     coords = new LatLng(location.getLatitude(), location.getLongitude());
                                 }

                                 if (circle != null) {
                                     circle.remove();
                                 }
                                 circle = googleMap.addCircle(new CircleOptions()
                                         .center(coords)
                                         .strokeWidth(0)
                                         .fillColor(ContextCompat.getColor(this, R.color.fresco_assignment_radius)));

                                 updateMapBounds();
                             });
    }

    public void killMe() {
        if (isTaskRoot()) {
            HomeActivity.start(this, true);
        }
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            setResult(KILL_ME);
            finishAndRemoveTask();
        }
        else {
            setResult(KILL_ME);
            finish();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //todo add snackbar

    }
}

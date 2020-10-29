package com.fresconews.fresco.v2.home;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.fresconews.fresco.BuildConfig;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.AuthManager;
import com.fresconews.fresco.framework.persistence.managers.FCMManager;
import com.fresconews.fresco.framework.persistence.managers.LocalSettingsManager;
import com.fresconews.fresco.framework.persistence.managers.SearchManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailActivity;
import com.fresconews.fresco.v2.onboarding.OnboardingActivity;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;

import io.smooch.core.Smooch;

public class HomeActivity extends BaseActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    public static final String EXTRA_ACTION_FROM_NOTIFICATION = "FROM_PUSH_NOTIFICATION";
    public static final String EXTRA_FORCE_REFRESH = "EXTRA_FORCE_REFRESH";

    @Inject
    LocalSettingsManager localSettingsManager;

    @Inject
    SessionManager sessionManager;

    @Inject
    AnalyticsManager analyticsManager;

    @Inject
    SearchManager searchManager;

    @Inject
    AuthManager authManager;

    public static void start(Context context, boolean clearTop) {
        start(context, clearTop, false);
    }

    public static void start(Context context, boolean clearTop, boolean forceRefresh) {
        Intent starter = new Intent(context, HomeActivity.class);
        if (clearTop) {
            starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        starter.putExtra(EXTRA_FORCE_REFRESH, forceRefresh);
        context.startActivity(starter);
    }

    public static String getBase64String(String value) throws UnsupportedEncodingException {
        return Base64.encodeToString(value.getBytes("UTF-8"), Base64.NO_WRAP);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);

        analyticsManager.checkPermissionChanges();
        analyticsManager.trackScreen("Home", "highlights");
        searchManager.clearSearchTerms();

        authManager.updateInstallation(false)
                   .onErrorReturn(throwable -> null).subscribe();

        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                AdvertisingIdClient.Info idInfo = null;
                try {
                    idInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
                }
                catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                String advertId = null;
                try {
                    advertId = idInfo.getId();
                }
                catch (NullPointerException e) {
                    e.printStackTrace(); //todo who wrote this??
                }

                return advertId;
            }

            @Override
            protected void onPostExecute(String advertId) {
                LogUtils.i(TAG, "device id - " + advertId);
            }

        };
        task.execute();

        // Only show the onboarding if this is the first run
        if (localSettingsManager.isFirstRun()) {
            localSettingsManager.setFirstRun(false);
            OnboardingActivity.start(this, "HOME");
        }

        HomeViewModel viewModel = new HomeViewModel(this, getIntent().getBooleanExtra(EXTRA_FORCE_REFRESH, false));
        setViewModel(R.layout.activity_home, viewModel);

        if (getIntent().getBooleanExtra(EXTRA_ACTION_FROM_NOTIFICATION, false)) {
            GalleryDetailActivity.start(this, getIntent().getExtras());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        analyticsManager.stopTrackingPost();
    }
}

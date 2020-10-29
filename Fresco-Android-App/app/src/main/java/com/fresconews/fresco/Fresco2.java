package com.fresconews.fresco;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.multidex.MultiDex;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.core.CrashlyticsCore;
import com.devbrackets.android.exomedia.core.exoplayer.EMExoPlayer;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.stetho.Stetho;
import com.fresconews.fresco.framework.databinding.bindingUtils.Objects;
import com.fresconews.fresco.framework.injection.components.DaggerFrescoComponent;
import com.fresconews.fresco.framework.injection.components.FrescoComponent;
import com.fresconews.fresco.framework.injection.modules.AppModule;
import com.fresconews.fresco.framework.injection.modules.ManagerModule;
import com.fresconews.fresco.framework.injection.modules.NetworkModule;
import com.fresconews.fresco.framework.network.EndpointHelper;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.framework.persistence.managers.PostManager;
import com.fresconews.fresco.framework.persistence.models.Gallery;
import com.fresconews.fresco.framework.persistence.models.Post;
import com.fresconews.fresco.framework.persistence.models.Post_Table;
import com.fresconews.fresco.framework.rx.RxBus;
import com.fresconews.fresco.messages.ActivePostChangedMessage;
import com.fresconews.fresco.v2.backgroundservice.FrescoLocationService;
import com.fresconews.fresco.v2.backgroundservice.OnClearFromRecentService;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.zendesk.sdk.model.access.AnonymousIdentity;
import com.zendesk.sdk.model.access.Identity;
import com.zendesk.sdk.network.impl.ZendeskConfig;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;
import io.smooch.core.Smooch;
import rx.Observable;
import rx.Subscription;

public class Fresco2 extends Application {
    protected static final String TAG = Fresco2.class.getSimpleName();

    public static Map<Integer, Typeface> fontCache = new HashMap<>();

    public static Fresco2 fresco2;
    public static String activePostIdInView;
    public static EMExoPlayer mediaPlayer = new EMExoPlayer();
    private static final String SEGMENT_APP_TOKEN = "bxk48kwhbx8g";

    private FrescoComponent frescoComponent;
    private static FrescoLocationService locationService;

    public static Subscription highlightsFollowingSubscription;

    @Inject
    AnalyticsManager analyticsManager;

    @Override
    public void onCreate() {
        super.onCreate();

        EndpointHelper.INSTANCE.init(this);

        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(EndpointHelper.currentEndpoint.twitterConsumerKey, EndpointHelper.currentEndpoint.twitterConsumerSecret);
        // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit, new Twitter(authConfig), new Answers());

//        //Zendesk FAQ
        ZendeskConfig.INSTANCE.init(this, EndpointHelper.currentEndpoint.zendeskEndpoint, EndpointHelper.currentEndpoint.zendeskUniqueId, EndpointHelper.currentEndpoint.zendeskClientSDK);
        Identity identity = new AnonymousIdentity.Builder().withNameIdentifier("Generic").build();
        ZendeskConfig.INSTANCE.setIdentity(identity);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
        Fresco.initialize(this);
        Smooch.init(this, EndpointHelper.currentEndpoint.smoochToken);

        fresco2 = this;

        FlowManager.init(new FlowConfig.Builder(this).openDatabasesOnInit(true).build());
        frescoComponent = DaggerFrescoComponent.builder()
                                               .appModule(new AppModule(this))
                                               .networkModule(new NetworkModule())
                                               .managerModule(new ManagerModule())
                                               .build();
        frescoComponent.inject(this);

        //Adjust (for tracking installs from ad campaigns
        String environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
        if (BuildConfig.DEBUG) {
            environment = AdjustConfig.ENVIRONMENT_SANDBOX;
        }
        AdjustConfig config = new AdjustConfig(this, SEGMENT_APP_TOKEN, environment);
        Adjust.onCreate(config);
        registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());

        Intent locationService = new Intent(getBaseContext(), FrescoLocationService.class);
        bindService(locationService, connection, Context.BIND_AUTO_CREATE);
        startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        analyticsManager.exitedOnboarding();
        unbindService(connection);
    }

    public static void startLocationService() {
        if (locationService != null) {
            locationService.startLocationListener();
        }
    }

    public static void stopLocationService() {
        if (locationService != null) {
            locationService.locationUnSubscribe();
        }
    }

    public FrescoComponent getFrescoComponent() {
        return frescoComponent;
    }

    public static void setActivePostIdInViewAnalytics(String postId, String galleryId, boolean inList) {
        setActivePostIdInViewAnalytics(postId, galleryId, inList, false);
    }

    public static void setActivePostIdInViewAnalytics(String postId, String galleryId, boolean inList, boolean force) {
        if (force || !Objects.equals(activePostIdInView, postId)) {
            activePostIdInView = postId;
            fresco2.analyticsManager.postSession(postId, galleryId, inList);
            mediaPlayer.stop();
            RxBus.getInstance().post(new ActivePostChangedMessage(postId));
        }
    }

    public static Context getContext() {
        return fresco2;
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            FrescoLocationService.LocationBinder binder = (FrescoLocationService.LocationBinder) service;
            locationService = binder.getService();
            startLocationService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    private static final class AdjustLifecycleCallbacks implements ActivityLifecycleCallbacks {
        @Override
        public void onActivityResumed(Activity activity) {
            Adjust.onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Adjust.onPause();
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        //...
    }
}

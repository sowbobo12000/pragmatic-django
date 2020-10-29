package com.fresconews.fresco.v2.backgroundservice;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.framework.persistence.managers.FrescoLocationManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.v2.utils.GoogleApiUtils;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Subscription;

/**
 * Created by Maurice on 07/09/2016.
 */
public class FrescoLocationService extends IntentService {
    private static final String TAG = "FrescoLocationService";
    private static final long LOCATION_MAX_INTERVAL = 60000L;
    private static final long LOCATION_FASTEST_INTERVAL = 4000L;
    private static final float MINIMUM_DISPLACEMENT = 60f;
    private static final float SUFFICIENT_ACCURACY = 600; //meters

    @Inject
    FrescoLocationManager frescoLocationManager;

    @Inject
    UserManager userManager;

    @Inject
    SessionManager sessionManager;

    private boolean isLocationStarted = false;
    private Subscription subscription;
    private IBinder binder = new LocationBinder();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public FrescoLocationService() {
        super("FrescoLocationService");
    }

    @Override
    public void onDestroy() {
        locationUnSubscribe();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);
        frescoLocationManager.setReactiveLocationProvider(new ReactiveLocationProvider(getApplicationContext()));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    public void startLocationListener() {
        if (!isLocationStarted && sessionManager.isLoggedIn()) {
            try {
                if (GoogleApiUtils.checkPlayServices(this)) {
                    isLocationStarted = true;
                    startLocation();
                }
            }
            catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void startLocation() {
        LogUtils.i(TAG, "starting Location..");
        LocationRequest request = LocationRequest.create()
                                                 .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                                                 .setInterval(LOCATION_MAX_INTERVAL)
                                                 .setMaxWaitTime(LOCATION_MAX_INTERVAL * 2)
                                                 .setSmallestDisplacement(MINIMUM_DISPLACEMENT)
                                                 .setFastestInterval(LOCATION_FASTEST_INTERVAL);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            subscription = frescoLocationManager.getReactiveLocationProvider()
                                                .getUpdatedLocation(request)
                                                .onErrorReturn(throwable -> {
                                                    LogUtils.e(TAG, "onErrorReturn", throwable);
                                                    locationUnSubscribe();
                                                    return null;
                                                })
                                                .filter(location -> location != null && location.getAccuracy() < SUFFICIENT_ACCURACY)    // you can filter location updates
                                                .subscribe(this::doObtainedLocation, Throwable::printStackTrace);
        }
    }

    public void locationUnSubscribe() {
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
        isLocationStarted = false;
    }

    // Updated every 60 seconds
    private void doObtainedLocation(final Location location) {
        LatLng userLocation = frescoLocationManager.getUserLocation();
        if (userLocation != null && location != null) {
            if (location.getLatitude() == userLocation.latitude && location.getLongitude() == userLocation.longitude) {
                LogUtils.d(TAG, "Location unchanged");
                return;
            }
        }
        if (sessionManager.isLoggedIn() && location != null) {
            LogUtils.d(TAG, "Location: " + Double.toString(location.getLatitude()));
            frescoLocationManager.setUserLocation(new LatLng(location.getLatitude(), location.getLongitude()));
            userManager.updateLocation((float) location.getLongitude(), (float) location.getLatitude());
        }
    }

    public class LocationBinder extends Binder {
        public FrescoLocationService getService() {
            return FrescoLocationService.this;
        }
    }
}

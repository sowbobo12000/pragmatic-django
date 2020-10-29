package com.fresconews.fresco.v2.settings.dialogs;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.databinding.Bindable;
import android.support.annotation.LayoutRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.mvvm.DialogViewModel;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.rx.RxGoogleMaps;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.Locale;

import javax.inject.Inject;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.functions.Action1;

/**
 * Created by mauricewu on 10/28/16.
 */
public class MapRadiusDialogViewModel extends DialogViewModel {
    private static final String TAG = MapRadiusDialogViewModel.class.getSimpleName();

    @Inject
    UserManager userManager;

    @Inject
    SessionManager sessionManager;

    private GoogleMap map;
    private Circle circle;
    private ReactiveLocationProvider locationProvider;
    private double notificationRadius = 0;
    private int progress;
    private boolean userUpdatedProgress = false;
    public double notificationRadiusMiles;
    public String notificationRadiusLabel;
    public boolean isShowing;

    private MapRadiusDialogListener listener;

    public MapRadiusDialogViewModel(Activity activity, MapRadiusDialogListener listener) {
        super(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        this.listener = listener;
        userUpdatedProgress = false;
        setNotificationRadiusLabel(String.format(Locale.getDefault(), "%d mi.", (int) notificationRadiusMiles));
        locationProvider = new ReactiveLocationProvider(activity);

        String userId = "";
        if (sessionManager.isLoggedIn() && sessionManager.getCurrentSession().getUserId() != null) {
            userId = sessionManager.getCurrentSession().getUserId();
        }
        userManager.getUser(userId)
                   .onErrorReturn(throwable -> null)
                   .filter(user -> user != null)
                   .subscribe(user -> {
                       String radStr = user.getRadius();
                       Double rad = .25;
                       if (radStr != null) {
                           rad = Double.parseDouble(radStr);
                           setNotificationRadiusLabel(String.format(Locale.getDefault(), "%d mi.", (int) Double.parseDouble(user.getRadius())));
                           setNotificationRadiusMiles(Double.parseDouble(user.getRadius()));
                           setProgress(getProgressFromRadius(Double.parseDouble(user.getRadius())));
                       }
                       this.notificationRadius = rad;
                   });
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }
        super.onBound();

        listener.onBound();

        SupportMapFragment mapFragment = ((SupportMapFragment) ((FragmentActivity) activity).getSupportFragmentManager().findFragmentById(R.id.map));

        RxGoogleMaps.getMap(mapFragment)
                    .map(googleMap -> {
                        map = googleMap;
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                        return map;
                    })
                    .compose(new RxPermissions(activity).ensure(android.Manifest.permission.ACCESS_FINE_LOCATION))
                    .onErrorReturn(throwable -> null)
                    .subscribe(granted -> {
                        if (granted != null && granted && map != null) {
                            //noinspection MissingPermission
                            map.setMyLocationEnabled(true);
                        }
                        initMap();
                    });
    }

    public void initMap() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationProvider.getLastKnownLocation()
                        .defaultIfEmpty(null)
                        .onErrorReturn(throwable -> null)
                        .subscribe(location -> {
                            LatLng coords;
                            if (location == null) {
                                coords = new LatLng(40.7128, -74.0059); //Default to NYC
                            }
                            else {
                                coords = new LatLng(location.getLatitude(), location.getLongitude());
                            }

                            if (circle != null) {
                                circle.remove();
                            }
                            circle = map.addCircle(new CircleOptions()
                                    .center(coords)
                                    .strokeWidth(0)
                                    .fillColor(ContextCompat.getColor(activity, R.color.fresco_assignment_radius_blue)));

                            updateMapBounds();
                        });
    }

    @Override
    public void show(@LayoutRes int layoutRes) {
        super.show(layoutRes);
        isShowing = true;
    }

    private void updateMapBounds() {
        if (circle == null) {
            return;
        }
        LatLng latLng = circle.getCenter();
        double radiusInMeters = notificationRadius * 1609.34;
        double viewRadiusMeters = radiusInMeters;

        if (viewRadiusMeters == 0) {
            viewRadiusMeters = 161; // 1/10th of a mile
        }

        LatLngBounds circleBounds = new LatLngBounds.Builder()
                .include(SphericalUtil.computeOffset(latLng, viewRadiusMeters, 0))
                .include(SphericalUtil.computeOffset(latLng, viewRadiusMeters, 90))
                .include(SphericalUtil.computeOffset(latLng, viewRadiusMeters, 180))
                .include(SphericalUtil.computeOffset(latLng, viewRadiusMeters, 270))
                .build();

        circle.setRadius(radiusInMeters);
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(circleBounds, 400, 400, 0));
    }

    public void setNotificationRadius(double mNotificationRadius) {
        this.notificationRadius = mNotificationRadius;
        updateMapBounds();
    }

    public Action1<Integer> radiusChanged = new Action1<Integer>() {
        @Override
        public void call(Integer progress) {
            userUpdatedProgress = true;
            notificationRadiusMiles = getRadiusFromProgress(progress);
            setNotificationRadiusLabel(String.format(Locale.getDefault(), "%d mi.", (int) notificationRadiusMiles));
            setNotificationRadius(notificationRadiusMiles);
            setProgress(progress);
        }
    };

    @Bindable
    public double getNotificationRadiusMiles() {
        return notificationRadiusMiles;
    }

    public void setNotificationRadiusMiles(double notificationRadiusMiles) {
        this.notificationRadiusMiles = notificationRadiusMiles;
        notifyPropertyChanged(BR.notificationRadiusMiles);
    }

    @Bindable
    public int getProgress() {
        //Sets progress on first open
        userManager.me().onErrorReturn(throwable -> null).subscribe(user -> {
            if (user != null && user.getRadius() != null) {
                double rad = Double.parseDouble(user.getRadius());
                if (progress == 0 && rad != 0 && !userUpdatedProgress) {
                    setProgress(getProgressFromRadius(rad));
                }
            }
        });
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        notifyPropertyChanged(BR.progress);
    }

    @Bindable
    public String getNotificationRadiusLabel() {
        return notificationRadiusLabel;
    }

    public void setNotificationRadiusLabel(String notificationRadiusLabel) {
        this.notificationRadiusLabel = notificationRadiusLabel;
        notifyPropertyChanged(BR.notificationRadiusLabel);
    }

    private double getRadiusFromProgress(int progress) {
        double radius = 49 * Math.pow((double) progress / 1000.0, 2.45) + 1;
        return radius;
    }

    public int getProgressFromRadius(double radiusInMiles) {
        int progress = 0;
        //y = 49(x)^2.45 + 1  //radius from progress if progress on a scale of 0-1
        //x = ( (y-1)/49 )^(1/2.45) //progress from radius where x will be on a scale of 0-1

        progress = (int) (1000 * Math.pow(((radiusInMiles - 1.0) / 49.0), (1.0 / 2.45)));

        return progress;
    }

    public int getProgressFromRadiusOld(double radiusInMiles) {
        int progress = 0;
        if (radiusInMiles > 20) {
            double amt = radiusInMiles - 20;
            progress += (int) amt;
            radiusInMiles -= amt;
        }
        if (radiusInMiles > 5) {
            double amt = radiusInMiles - 5;
            progress += (int) (amt * 2);
            radiusInMiles -= amt;
        }
        if (radiusInMiles > 1) {
            double amt = radiusInMiles - 1;
            progress += (int) (amt * 4);
            radiusInMiles -= amt;
        }

        progress += radiusInMiles * 10;

        return progress;

    }

    public Action1<View> sConsumeMapTouch = view -> {
        //Consumed view touch event here,which is over map.
    };

    public Action1<View> cancel = view -> {
        if (dialog != null) {
            dialog.dismiss();
        }
    };

    public Action1<View> save = view -> {
        if (dialog != null) {
            dialog.dismiss();
        }
        if (listener != null) {
            listener.onSaveRadiusClick(notificationRadius);
        }
    };

    public interface MapRadiusDialogListener {
        void onBound();
        void onSaveRadiusClick(Double radius);
    }
}

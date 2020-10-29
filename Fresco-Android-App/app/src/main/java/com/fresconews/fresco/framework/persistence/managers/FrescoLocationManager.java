package com.fresconews.fresco.framework.persistence.managers;

import com.google.android.gms.maps.model.LatLng;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;

/**
 * Created by Maurice on 07/09/2016.
 */
public class FrescoLocationManager {
    private LatLng userLocation;
    private ReactiveLocationProvider reactiveLocationProvider;

    public LatLng getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(LatLng userLocation) {
        this.userLocation = userLocation;
    }

    public ReactiveLocationProvider getReactiveLocationProvider() {
        return reactiveLocationProvider;
    }

    public void setReactiveLocationProvider(ReactiveLocationProvider reactiveLocationProvider) {
        this.reactiveLocationProvider = reactiveLocationProvider;
    }

}

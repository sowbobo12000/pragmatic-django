package com.fresconews.fresco.framework.rx;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import rx.Observable;

public class RxGoogleMaps {
    public static Observable<GoogleMap> getMap(SupportMapFragment mapFragment) {
        return Observable.create(subscriber -> {
            mapFragment.getMapAsync(googleMap -> {
                subscriber.onNext(googleMap);
                subscriber.onCompleted();
            });
        });
    }

    public static Observable<CameraPosition> onCameraChanged(GoogleMap map) {
        return Observable.create(subscriber -> {
            map.setOnCameraChangeListener(subscriber::onNext);
        });
    }

    public static Observable<Marker> onMarkerClicked(GoogleMap map) {
        return Observable.create(subscriber -> {
            map.setOnMarkerClickListener(marker -> {
                subscriber.onNext(marker);
                return true;
            });
        });
    }
}

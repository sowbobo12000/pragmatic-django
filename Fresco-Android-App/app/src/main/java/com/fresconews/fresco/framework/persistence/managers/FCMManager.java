package com.fresconews.fresco.framework.persistence.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.fresconews.fresco.v2.utils.LogUtils;
import com.google.firebase.iid.FirebaseInstanceId;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by ryan on 6/22/2016.
 */
public class FCMManager {
    private static final String TAG = FCMManager.class.getSimpleName();
    private static final String DEVICE_TOKEN_PREF = "DEVICE_TOKEN_PREF";
    private static final String GENEREATED_DEVICE_TOKEN_KEY = "GENERATED_DEVICE_TOKEN";

    private Context context;
    private SharedPreferences sharedpreferences;

    public FCMManager(Context context) {
        this.context = context;
        this.sharedpreferences = context.getSharedPreferences(DEVICE_TOKEN_PREF, Context.MODE_PRIVATE);
    }

    public Observable<String> getDeviceToken() {
        Observable<String> tokenObservable = Observable.create(subscriber -> {
            String token = FirebaseInstanceId.getInstance().getToken();
            subscriber.onNext(token);
            subscriber.onCompleted();
        });

        return tokenObservable.subscribeOn(Schedulers.io());
    }

    public String getDeviceId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}

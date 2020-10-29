package com.fresconews.fresco.framework.rx;

import android.content.Context;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * Created by ryan on 7/20/2016.
 */
public class RxOrientationEventListener implements Observable.OnSubscribe<Integer> {
    private OrientationEventListener mListener;

    Subscriber<? super Integer> mSubscriber;

    public RxOrientationEventListener(Context context, int sensorDelay) {
        mListener = new OrientationEventListener(context, sensorDelay) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (mSubscriber != null && !mSubscriber.isUnsubscribed()) {
                    mSubscriber.onNext(orientation);
                }
            }
        };
    }

    public void enable() {
        mListener.enable();
    }

    public void disable() {
        mListener.disable();
        mSubscriber.onCompleted();
    }

    @Override
    public void call(Subscriber<? super Integer> subscriber) {
        mSubscriber = subscriber;
    }
}

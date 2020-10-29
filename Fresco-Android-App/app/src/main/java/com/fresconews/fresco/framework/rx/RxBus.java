package com.fresconews.fresco.framework.rx;

import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class RxBus {
    private static RxBus INSTANCE;

    public static RxBus getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RxBus();
        }
        return INSTANCE;
    }

    private Subject<Object, Object> mSubject = new SerializedSubject<>(PublishSubject.create());

    public <T> Subscription register(Class<T> eventClass, Action1<T> action) {
        return mSubject
                .throttleLast(300, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .filter(event -> event.getClass().equals(eventClass))
                .map(obj -> (T) obj)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action);
    }

    public void post(Object event) {
        mSubject.onNext(event);
    }
}

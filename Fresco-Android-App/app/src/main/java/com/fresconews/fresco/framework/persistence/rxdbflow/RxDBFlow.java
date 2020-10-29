package com.fresconews.fresco.framework.persistence.rxdbflow;

import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;

import rx.Observable;
import rx.android.MainThreadSubscription;

public class RxDBFlow {

    public static <T extends Model> Observable<BaseModel.Action> onTableChanged(FlowQueryList<T> queryList) {
        Observable.OnSubscribe<BaseModel.Action> onSubscribe = subscriber -> {
            FlowContentObserver.OnTableChangedListener listener = (tableChanged, action) -> {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(action);
                }
            };
            queryList.addOnTableChangedListener(listener);
            subscriber.add(new MainThreadSubscription() {
                @Override
                protected void onUnsubscribe()
                {
                    queryList.removeTableChangedListener(listener);
                }
            });
        };
        return Observable.create(onSubscribe).onBackpressureBuffer();
    }
}

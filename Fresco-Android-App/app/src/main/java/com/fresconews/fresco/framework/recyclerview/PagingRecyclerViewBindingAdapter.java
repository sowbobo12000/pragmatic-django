package com.fresconews.fresco.framework.recyclerview;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.framework.mvvm.ViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;
import rx.functions.Action1;

/**
 * Created by Ryan on 6/1/2016.
 */
public class PagingRecyclerViewBindingAdapter<T extends ViewModel> extends RecyclerViewBindingAdapter<T> implements Observable.OnSubscribe<T> {
    private static final String TAG = PagingRecyclerViewBindingAdapter.class.getSimpleName();

    private Action1<T> newPageListener;

    private int lastPagingPosition = -1;
    private int offset;

    public PagingRecyclerViewBindingAdapter(@LayoutRes int itemLayout, IDataSource<T> dataSource) {
        super(itemLayout, dataSource);
        this.dataSource = dataSource;
        offset = 1;
    }

    @Override
    public void onBindViewHolder(BindingViewHolder<T> holder, int position) {
        super.onBindViewHolder(holder, position);

        if (position >= getItemCount() - offset && position > lastPagingPosition) {
            if (newPageListener != null) {
                newPageListener.call(getDataSource().get(position));
            }
            lastPagingPosition = position;
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (dataSource != null) {
            dataSource.onDetached();
        }
    }

    public Observable<T> getNewPageObservable() {
        return Observable.create(this);
    }

    private void setNewPageListener(Action1<T> newPageListener) {
        this.newPageListener = newPageListener;
    }

    public void resetLastPagingPosition() {
        lastPagingPosition = -1;
    }

    protected void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        MainThreadSubscription.verifyMainThread();

        subscriber.onNext(null);

        setNewPageListener((T item) -> {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(item);
            }
        });

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                newPageListener = null;
            }
        });
    }

}
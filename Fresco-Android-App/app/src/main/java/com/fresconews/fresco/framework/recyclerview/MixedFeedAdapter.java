package com.fresconews.fresco.framework.recyclerview;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fresconews.fresco.framework.mvvm.ItemViewModel;
import com.fresconews.fresco.framework.mvvm.ViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.mvvm.viewmodels.GalleryViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.StoriesPreviewItemModel;

import java.util.Date;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;
import rx.functions.Action1;

public class MixedFeedAdapter extends RecyclerViewBindingAdapter<ItemViewModel> implements Observable.OnSubscribe<Date> {
    private static final String TAG = MixedFeedAdapter.class.getSimpleName();

    private static final int GALLERY_TYPE = 1;
    private static final int STORY_TYPE = 2;

    @LayoutRes
    private int galleryLayout;

    @LayoutRes
    private int storyLayout;

    private Action1<ViewModel> newPageListener;
    private int lastPagingPosition = -1;

    public MixedFeedAdapter(@LayoutRes int galleryLayout, @LayoutRes int storyLayout, IDataSource<ItemViewModel> dataSource) {
        this.galleryLayout = galleryLayout;
        this.storyLayout = storyLayout;
        setDataSource(dataSource);
    }

    @Override
    public int getItemViewType(int position) {
        if (dataSource.get(position) instanceof GalleryViewModel) {
            return GALLERY_TYPE;
        }
        else {
            return STORY_TYPE;
        }
    }

    @Override
    public BindingViewHolder<ItemViewModel> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case GALLERY_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(galleryLayout, parent, false);
                break;
            case STORY_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(storyLayout, parent, false);
                break;
        }

        return new BindingViewHolder<>(view);
    }

    @Override
    public void onBindViewHolder(BindingViewHolder<ItemViewModel> holder, int position) {
        super.onBindViewHolder(holder, position);

        if (position >= getItemCount() - 1 && position > lastPagingPosition) {
            if (newPageListener != null) {
                newPageListener.call(getDataSource().get(position));
            }
            lastPagingPosition = position;
        }
    }

    public void resetLastPagingPosition() {
        lastPagingPosition = -1;
    }

    public Observable<Date> getNewPageObservable() {
        return Observable.create(this);
    }

    @Override
    public void call(Subscriber<? super Date> subscriber) {
        MainThreadSubscription.verifyMainThread();

        subscriber.onNext(null);

        newPageListener = (ViewModel item) -> {
            if (!subscriber.isUnsubscribed()) {
                Date pageKey = new Date();
                if (item instanceof GalleryViewModel) {
                    GalleryViewModel galleryViewModel = (GalleryViewModel) item;
                    pageKey = galleryViewModel.getActionAt();
                }
                else if (item instanceof StoriesPreviewItemModel) {
                    StoriesPreviewItemModel storyViewModel = (StoriesPreviewItemModel) item;
                    if (storyViewModel.getItem() == null) {
                        return; //this happened to me after refreshing highlights and switching to stories, then refreshing stories. ? -Blaze
                    }
                    pageKey = storyViewModel.getItem().getActionAt();
                }
                subscriber.onNext(pageKey);
            }
        };

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                newPageListener = null;
            }
        });
    }
}

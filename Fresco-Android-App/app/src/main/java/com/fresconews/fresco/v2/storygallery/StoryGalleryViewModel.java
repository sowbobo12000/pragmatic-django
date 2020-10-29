package com.fresconews.fresco.v2.storygallery;

import android.databinding.Bindable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.GalleryViewModelDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.mvvm.viewmodels.GalleryViewModel;
import com.fresconews.fresco.framework.persistence.managers.StoryManager;
import com.fresconews.fresco.framework.recyclerview.PagingRecyclerViewBindingAdapter;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.fresconews.fresco.v2.utils.VideoScrollListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by techjini on 11/7/16.
 */
public class StoryGalleryViewModel extends ActivityViewModel<StoryGalleryActivity> {
    private static final String TAG = StoryGalleryViewModel.class.getSimpleName();

    private static final int PAGE_SIZE = 10;

    @Inject
    StoryManager storyManager;

    private IDataSource<GalleryViewModel> dataSource;
    private PagingRecyclerViewBindingAdapter<GalleryViewModel> adapter;
    private String storyId;
    private String storyCaption;
    private boolean captionFilled;
    private boolean emptyState;

    StoryGalleryViewModel(StoryGalleryActivity activity, String paramTitle, String storyId, String storyCaption) {
        super(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        setTitle(paramTitle);
        setNavIcon(R.drawable.ic_navigation_arrow_back_white);

        this.storyId = storyId;
        this.storyCaption = storyCaption;

        if (TextUtils.isEmpty(storyCaption)) {
            storyManager.getOrDownloadStory(storyId).onErrorReturn(throwable -> null).subscribe(story -> {
                if (story == null) {
                    return;
                }
                this.storyCaption = story.getCaption();
                setTitle(story.getTitle());
                notifyPropertyChanged(BR.caption);
                notifyPropertyChanged(BR.captionFilled);
            });
        }
    }

    public Action1<RecyclerView> onRecyclerViewCreated = recyclerView -> {
        dataSource = new GalleryViewModelDataSource(getActivity(), storyManager.getGalleryDatasource(storyId));
        adapter = new PagingRecyclerViewBindingAdapter<>(R.layout.item_gallery, dataSource);
        adapter.getNewPageObservable()
               .onErrorReturn(throwable -> null)
               .subscribe(last -> {
                   if (last == null) {
                       storyManager.downloadGalleries(storyId, PAGE_SIZE)
                                   .observeOn(AndroidSchedulers.mainThread())
                                   .subscribeOn(Schedulers.io())
                                   .onErrorReturn(throwable -> {
                                       setEmptyState(true);
                                       if (getActivity() != null) {
                                           SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_galleries);
                                       }
                                       return null;
                                   })
                                   .subscribe(galleries -> {
                                       adapter.notifyDataSetChanged();
                                       setEmptyState(adapter.getItemCount() == 0 && (galleries == null || galleries.isEmpty()));
                                   });
                   }
                   else {
                       storyManager.downloadGalleries(storyId, PAGE_SIZE, last.getId())
                                   .observeOn(AndroidSchedulers.mainThread())
                                   .subscribeOn(Schedulers.io())
                                   .onErrorReturn(throwable -> {
                                       if (getActivity() != null) {
                                           SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_galleries);
                                       }
                                       return null;
                                   })
                                   .filter(galleries -> galleries != null)
                                   .subscribe(galleries -> {
                                       adapter.notifyItemRangeChanged(adapter.getItemCount() - galleries.size(), galleries.size());
                                   });
                   }
               });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new VideoScrollListener<>(dataSource));
    };

    public Action1<SwipeRefreshLayout> refresh = swipeRefreshLayout -> {
        List<GalleryViewModel> list = new ArrayList<>();
        if (dataSource != null) {
            list.addAll(dataSource.list());
        }
        storyManager.clearStoryGalleries();
        adapter.resetLastPagingPosition();

        storyManager.downloadGalleries(storyId, PAGE_SIZE)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .onErrorReturn(throwable -> {
                        setEmptyState(true);
                        if (getActivity() != null) {
                            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_galleries);
                        }
                        return null;
                    })
                    .subscribe(galleries -> {
                        adapter.refresh(list, galleries, PAGE_SIZE);
                        setEmptyState(adapter.getItemCount() == 0 && (galleries == null || galleries.isEmpty()));
                        swipeRefreshLayout.setRefreshing(false);
                    });
    };

    public String getCaption() {
        return storyCaption;
    }

    @Bindable
    public boolean isCaptionFilled() {
        return !TextUtils.isEmpty(storyCaption);
    }

    @Bindable
    public boolean isEmptyState() {
        return emptyState;
    }

    public void setEmptyState(boolean emptyState) {
        this.emptyState = emptyState;
        notifyPropertyChanged(BR.emptyState);
    }
}

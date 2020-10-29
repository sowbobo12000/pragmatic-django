package com.fresconews.fresco.v2.profile.user;

import android.databinding.Bindable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.FragmentViewModel;
import com.fresconews.fresco.framework.mvvm.ItemViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.MixedFeedDataSource;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.FeedManager;
import com.fresconews.fresco.framework.recyclerview.MixedFeedAdapter;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.fresconews.fresco.v2.utils.VideoScrollListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class UserFeedViewModel extends FragmentViewModel<UserFeedFragment> {
    private static final String TAG = UserFeedViewModel.class.getSimpleName();
    private static final int PAGE_SIZE = 10;

    @Inject
    FeedManager feedManager;

    @Inject
    AnalyticsManager analyticsManager;

    public BindableView<RecyclerView> recyclerView = new BindableView<>();

    private IDataSource<ItemViewModel> dataSource;
    private MixedFeedAdapter adapter;
    private String userId;
    private boolean emptyState = false;

    public UserFeedViewModel(UserFeedFragment fragment, String userId) {
        super(fragment);
        this.userId = userId;
        ((Fresco2) getActivity().getApplication()).getFrescoComponent().inject(this);
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }

        super.onBound();

        feedManager.clearUserFeed(userId);

        dataSource = new MixedFeedDataSource(getActivity(), feedManager.userFeedDataSource(userId));
        adapter = new MixedFeedAdapter(R.layout.item_gallery, R.layout.item_stories_preview, dataSource);
        adapter.getNewPageObservable().onErrorReturn(throwable -> null).subscribe(last -> {
            if (last == null) {
                feedManager.user(userId, PAGE_SIZE)
                           .subscribeOn(Schedulers.io())
                           .observeOn(AndroidSchedulers.mainThread())
                           .onErrorReturn(throwable -> {
                               setEmptyState(true);
                               if (getRoot() != null) {
                                   SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_user_content);
                               }
                               return null;
                           })
                           .subscribe(baseModels -> {
                               adapter.notifyDataSetChanged();
                               setEmptyState(adapter.getItemCount() == 0 && (baseModels == null || baseModels.isEmpty()));
                               if (baseModels != null && !baseModels.isEmpty()) {
                                   analyticsManager.galleriesScrolledByInProfile(baseModels.size());
                               }
                           });
            }
            else {
                feedManager.user(userId, PAGE_SIZE, last)
                           .subscribeOn(Schedulers.io())
                           .observeOn(AndroidSchedulers.mainThread())
                           .onErrorReturn(throwable -> {
                               if (getRoot() != null) {
                                   SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_user_content);
                               }
                               return null;
                           })
                           .subscribe(baseModels -> {
                               setEmptyState(adapter.getItemCount() == 0 && (baseModels == null || baseModels.isEmpty()));
                               if (baseModels != null && !baseModels.isEmpty()) {
                                   analyticsManager.galleriesScrolledByInProfile(baseModels.size());
                                   adapter.notifyItemRangeChanged(adapter.getItemCount() - baseModels.size(), baseModels.size());
                               }
                           });
            }
        });

        recyclerView.get().setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.get().setAdapter(adapter);
        recyclerView.get().addOnScrollListener(new VideoScrollListener<>(dataSource));
    }

    public Action1<SwipeRefreshLayout> refresh = swipeRefreshLayout -> {
        List<ItemViewModel> list = new ArrayList<>();
        if (dataSource != null) {
            list.addAll(dataSource.list());
        }
        feedManager.clearUserFeed(userId);
        adapter.resetLastPagingPosition();
        feedManager.user(userId, PAGE_SIZE)
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .onErrorReturn(throwable -> {
                       setEmptyState(true);
                       SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_user_content);
                       return null;
                   })
                   .subscribe(baseModels -> {
                       adapter.refresh(list, baseModels, PAGE_SIZE);
                       swipeRefreshLayout.setRefreshing(false);
                       setEmptyState(adapter.getItemCount() == 0 && (baseModels == null || baseModels.isEmpty()));
                   });
    };

    @Bindable
    public boolean getEmptyState() {
        return emptyState;
    }

    public void setEmptyState(boolean emptyState) {
        this.emptyState = emptyState;
        notifyPropertyChanged(BR.emptyState);
    }
}

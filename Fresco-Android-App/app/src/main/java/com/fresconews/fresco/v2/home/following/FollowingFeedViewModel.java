package com.fresconews.fresco.v2.home.following;

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
import com.fresconews.fresco.framework.persistence.managers.FeedManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.models.Session;
import com.fresconews.fresco.framework.recyclerview.MixedFeedAdapter;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.fresconews.fresco.v2.utils.VideoScrollListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class FollowingFeedViewModel extends FragmentViewModel<FollowingFeedFragment> {
    private static final String TAG = FollowingFeedViewModel.class.getSimpleName();

    private static final int PAGE_SIZE = 10;

    @Inject
    FeedManager feedManager;

    @Inject
    SessionManager sessionManager;

    public BindableView<RecyclerView> recyclerView = new BindableView<>();

    private IDataSource<ItemViewModel> dataSource;
    private MixedFeedAdapter adapter;
    private boolean emptyState = false;

    public FollowingFeedViewModel(FollowingFeedFragment fragment) {
        super(fragment);
        ((Fresco2) getActivity().getApplication()).getFrescoComponent().inject(this);
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }

        super.onBound();

        Session session = sessionManager.getCurrentSession();
        if (session == null || !sessionManager.isLoggedIn()) {
            return;
        }

        feedManager.clearFollowingFeed(session.getUserId());

        dataSource = new MixedFeedDataSource(getActivity(), feedManager.followingFeedDataSource(session.getUserId()), true);
        if (adapter == null) {
            adapter = new MixedFeedAdapter(R.layout.item_gallery, R.layout.item_stories_preview, dataSource);
        }
        else {
            adapter.notifyDataSetChanged();
        }

        adapter.getNewPageObservable().onErrorReturn(throwable -> null).subscribe(last -> {
            if (last == null) {
                feedManager.following(session.getUserId(), PAGE_SIZE)
                           .observeOn(AndroidSchedulers.mainThread())
                           .subscribeOn(Schedulers.io())
                           .onErrorReturn(throwable -> {
                               setEmptyState(true);
                               SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_following);
                               return null;
                           })
                           .subscribe(baseModels -> {
                               adapter.notifyDataSetChanged();
                               setEmptyState(adapter.getItemCount() == 0 && (baseModels == null || baseModels.isEmpty()));
                           });
            }
            else {
                feedManager.following(session.getUserId(), PAGE_SIZE, last)
                           .observeOn(AndroidSchedulers.mainThread())
                           .subscribeOn(Schedulers.io())
                           .onErrorReturn(throwable -> {
                               SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_following);
                               return null;
                           })
                           .filter(baseModels -> baseModels != null)
                           .subscribe(baseModels -> {
                               adapter.notifyItemRangeChanged(adapter.getItemCount() - baseModels.size(), baseModels.size());
                           });
            }
        });

        if (recyclerView.get().getAdapter() == null) {
            recyclerView.get().setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.get().setAdapter(adapter);
            recyclerView.get().addOnScrollListener(new VideoScrollListener<>(dataSource));
        }
    }

    public void reload(SwipeRefreshLayout swipeRefreshLayout) {
        Session session = sessionManager.getCurrentSession();
        if (session == null || !sessionManager.isLoggedIn() || adapter == null) {
            return;
        }
        List<ItemViewModel> list = new ArrayList<>();
        if (dataSource != null) {
            list.addAll(dataSource.list());
        }
        feedManager.clearFollowingFeed(session.getUserId());
        adapter.resetLastPagingPosition();
        feedManager.following(session.getUserId(), PAGE_SIZE)
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribeOn(Schedulers.io())
                   .onErrorReturn(throwable -> {
                       setEmptyState(true);
                       SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_following);
                       return null;
                   })
                   .subscribe(baseModels -> {
                       adapter.refresh(list, baseModels, PAGE_SIZE);
                       if (swipeRefreshLayout != null) {
                           swipeRefreshLayout.setRefreshing(false);
                       }
                       setEmptyState(adapter.getItemCount() == 0 && (baseModels == null || baseModels.isEmpty()));
                   });
    }

    public Action1<SwipeRefreshLayout> refresh = this::reload;

    @Bindable
    public boolean getEmptyState() {
        return emptyState;
    }

    public void setEmptyState(boolean emptyState) {
        this.emptyState = emptyState;
        notifyPropertyChanged(BR.emptyState);
    }
}

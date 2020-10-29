package com.fresconews.fresco.v2.search;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.UserSearchViewModelDataSource;
import com.fresconews.fresco.framework.mvvm.viewmodels.UserSearchViewModel;
import com.fresconews.fresco.framework.persistence.managers.SearchManager;
import com.fresconews.fresco.framework.persistence.models.SearchQuery;
import com.fresconews.fresco.framework.recyclerview.PagingRecyclerViewBindingAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Blaze on 8/4/2016.
 */
public class UserSeeAllViewModel extends ActivityViewModel<UserSeeAllActivity> {
    private static final String TAG = UserSeeAllViewModel.class.getSimpleName();
    private static final int PAGE_SIZE = 20;

    @Inject
    SearchManager searchManager;

    private SearchQuery searchQuery;
    private IDataSource<UserSearchViewModel> dataSource;
    private PagingRecyclerViewBindingAdapter<UserSearchViewModel> adapter;

    UserSeeAllViewModel(UserSeeAllActivity activity, SearchQuery searchQuery) {
        super(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);
        this.searchQuery = searchQuery;

        setTitle(activity.getString(R.string.all_users));
        setNavIcon(R.drawable.ic_navigation_arrow_back_white);
    }

    public Action1<View> onNavIconClicked = view -> {
        getActivity().onBackPressed();
    };

    public Action1<RecyclerView> onRecyclerViewCreated = recyclerView -> {
        dataSource = new UserSearchViewModelDataSource(getActivity(), searchManager.getUserListDataSource());

        adapter = new PagingRecyclerViewBindingAdapter<>(R.layout.item_user, dataSource);
        adapter.getNewPageObservable().onErrorReturn(throwable -> null).subscribe(last -> {
            if (last != null) {
                searchManager.downloadUsers(searchQuery, PAGE_SIZE, last.getId())
                             .observeOn(AndroidSchedulers.mainThread())
                             .subscribeOn(Schedulers.io())
                             .onErrorReturn(throwable -> null)
                             .subscribe(users -> {
                                 adapter.notifyDataSetChanged();
                             });
            }
            else {
                searchManager.downloadUsers(searchQuery, PAGE_SIZE)
                             .observeOn(AndroidSchedulers.mainThread())
                             .subscribeOn(Schedulers.io())
                             .filter(users -> users != null)
                             .onErrorReturn(throwable -> null)
                             .subscribe(users -> {
                                 if (users != null) {
                                     adapter.notifyItemRangeChanged(adapter.getItemCount() - users.size(), users.size());
                                 }
                             });
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    };

    public Action1<SwipeRefreshLayout> refresh = swipeRefreshLayout -> {
        List<UserSearchViewModel> list = new ArrayList<>();
        if (dataSource != null) {
            list.addAll(dataSource.list());
        }
        searchManager.clearSearchTerms();
        adapter.resetLastPagingPosition();
        searchManager.downloadUsers(searchQuery, PAGE_SIZE)
                     .observeOn(AndroidSchedulers.mainThread())
                     .subscribeOn(Schedulers.io())
                     .onErrorReturn(throwable -> null)
                     .subscribe(users -> {
                         swipeRefreshLayout.setRefreshing(false);
                         adapter.refresh(list, users, PAGE_SIZE);
                     });
    };
}

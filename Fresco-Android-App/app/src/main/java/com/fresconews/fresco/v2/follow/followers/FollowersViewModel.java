package com.fresconews.fresco.v2.follow.followers;

import android.databinding.Bindable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.FragmentViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.ListDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.UserSearchViewModelDataSource;
import com.fresconews.fresco.framework.mvvm.viewmodels.UserSearchViewModel;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.Gallery;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.framework.recyclerview.PagingRecyclerViewBindingAdapter;
import com.fresconews.fresco.v2.utils.SnackbarUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class FollowersViewModel extends FragmentViewModel<FollowersFragment> {
    private static final String TAG = FollowersViewModel.class.getSimpleName();
    private static final int PAGE_SIZE = 15;

    @Inject
    UserManager userManager;

    @Inject
    SessionManager sessionManager;

    @Inject
    GalleryManager galleryManager;

    public BindableView<RecyclerView> recyclerView = new BindableView<>();

    private String userId;
//    private boolean fromGallery;
    private ArrayList<User> users = null;
    private IDataSource<UserSearchViewModel> dataSource;
    private PagingRecyclerViewBindingAdapter<UserSearchViewModel> adapter;
    private boolean emptyState = false;

    private int state;
    private static final int GALLERY_LIKES_STATE = 11;
    private static final int GALLERY_REPOSTS_STATE = 12;

    public FollowersViewModel(FollowersFragment fragment, String userId, int state) {
        super(fragment);
        this.userId = userId;
        this.state = state;
        ((Fresco2) getActivity().getApplication()).getFrescoComponent().inject(this);
        users = new ArrayList<>();
    }

    @Bindable
    public boolean getEmptyState() {
        return emptyState;
    }

    public void setEmptyState(boolean emptyState) {
        this.emptyState = emptyState;
        notifyPropertyChanged(BR.emptyState);
    }

    public void detachDataSource() {
        //For help with SQLiteConnectionPool //endTransaction //close also work
        dataSource.onDetached();
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }

        super.onBound();

        if (recyclerView == null || recyclerView.get() == null) {
            return;
        }

        if (dataSource == null) {
            dataSource = new UserSearchViewModelDataSource(getFragment(), new ListDataSource<>(users));
        }
        if (adapter == null) {
            adapter = new PagingRecyclerViewBindingAdapter<>(R.layout.item_user, dataSource);
        }
        else {
            adapter.notifyDataSetChanged();
        }
        users.clear();
        adapter.getNewPageObservable().onErrorReturn(throwable -> null).subscribe(last -> {
            if (last == null) {
                if(state == GALLERY_LIKES_STATE || state == GALLERY_REPOSTS_STATE){
                    galleryManager.usersLiked(userId, PAGE_SIZE)
                            .onErrorReturn(throwable -> null)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(networkUsers -> {
                                setEmptyState((networkUsers == null || networkUsers.isEmpty()) && adapter.getItemCount() == 0);
                                users.clear();
                                if (networkUsers != null) {
                                    users.addAll(networkUsers);
                                }
                                adapter.notifyDataSetChanged();
                            });
                }else {
                    userManager.followers(userId, PAGE_SIZE)
                            .onErrorReturn(throwable -> null)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(networkUsers -> {
                                setEmptyState((networkUsers == null || networkUsers.isEmpty()) && adapter.getItemCount() == 0);
                                users.clear();
                                if (networkUsers != null) {
                                    users.addAll(networkUsers);
                                }
                                adapter.notifyDataSetChanged();
                            });
                }
            }
            else {
                if(state == GALLERY_LIKES_STATE || state == GALLERY_REPOSTS_STATE){
                    galleryManager.usersLiked(userId, PAGE_SIZE, last.getId())
                            .observeOn(AndroidSchedulers.mainThread())
                            .onErrorReturn(throwable -> {
                                if (getActivity() != null) {
                                    SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_reposts);
                                }
                                return null;
                            })
                            .subscribe(networkUsers -> {
                                if (networkUsers != null) {
                                    users.addAll(networkUsers);
                                    adapter.notifyItemRangeChanged(adapter.getItemCount() - networkUsers.size(), networkUsers.size());
                                }
                            });
                }else {
                    userManager.followers(userId, PAGE_SIZE, last.getId())
                            .observeOn(AndroidSchedulers.mainThread())
                            .onErrorReturn(throwable -> {
                                if (getActivity() != null) {
                                    SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_followers);
                                }
                                return null;
                            })
                            .subscribe(networkUsers -> {
                                if (networkUsers != null) {
                                    users.addAll(networkUsers);
                                    adapter.notifyItemRangeChanged(adapter.getItemCount() - networkUsers.size(), networkUsers.size());
                                }
                            });
                }
            }
        });

        recyclerView.get().setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.get().setAdapter(adapter);
    }

    public Action1<SwipeRefreshLayout> refresh = swipeRefreshLayout -> {
        List<UserSearchViewModel> list = new ArrayList<>();
        if (dataSource != null) {
            list.addAll(dataSource.list());
        }
        if (dataSource == null) {
            dataSource = new UserSearchViewModelDataSource(getFragment(), new ListDataSource<>(users));
        }
        if (adapter == null) {
            adapter = new PagingRecyclerViewBindingAdapter<>(R.layout.item_user, dataSource);
        }
        else {
            adapter.resetLastPagingPosition();
        }
        if(state == GALLERY_LIKES_STATE || state == GALLERY_REPOSTS_STATE){
            users.clear();
            galleryManager.usersLiked(userId, PAGE_SIZE)
                    .onErrorReturn(throwable -> null)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(networkUsers -> {
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        adapter.refresh(list, networkUsers, PAGE_SIZE);
                        setEmptyState((networkUsers == null || networkUsers.isEmpty()) && adapter.getItemCount() == 0);
                        if (networkUsers == null) {
                            users.clear();
                        } else {
                            users.addAll(networkUsers);
                            for(int i = 0; i<networkUsers.size(); i++){
                                User newCall = networkUsers.get(i);
                                if(i<dataSource.list().size()){
                                    if(dataSource.get(i).isFollowing() != newCall.isFollowing()){
                                        dataSource.get(i).getItem().setFollowing(newCall.isFollowing());
                                        adapter.notifyItemChanged(i);
                                    }
                                }
                            }
                        }
                    });
        }else {
            users.clear();
            userManager.followers(userId, PAGE_SIZE)
                    .onErrorReturn(throwable -> null)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(networkUsers -> {
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        adapter.refresh(list, networkUsers, PAGE_SIZE);
                        setEmptyState((networkUsers == null || networkUsers.isEmpty()) && adapter.getItemCount() == 0);
                        if (networkUsers == null) {
                            users.clear();
                        } else {
                            users.addAll(networkUsers);
                        }
                    });
        }
    };
}

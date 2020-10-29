package com.fresconews.fresco.v2.storiespreview;

import android.databinding.Bindable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.StoriesPreviewViewItemModelDataSource;
import com.fresconews.fresco.framework.mvvm.viewmodels.StoriesPreviewItemModel;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.NotificationFeedManager;
import com.fresconews.fresco.framework.persistence.managers.SearchManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.StoryManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.SearchQuery;
import com.fresconews.fresco.framework.persistence.models.Session;
import com.fresconews.fresco.framework.recyclerview.PagingRecyclerViewBindingAdapter;
import com.fresconews.fresco.v2.notificationfeed.NotificationFeedActivity;
import com.fresconews.fresco.v2.notifications.INotificationActivityViewModel;
import com.fresconews.fresco.v2.utils.SnackbarUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Khal on 4/7/16.
 */
public class StoriesPreviewViewModel extends ActivityViewModel<StoriesPreviewActivity> implements INotificationActivityViewModel {
    private static final String TAG = StoriesPreviewViewModel.class.getSimpleName();
    private static final int PAGE_SIZE = 10;

    @Inject
    SessionManager sessionManager;

    @Inject
    StoryManager storyManager;

    @Inject
    SearchManager searchManager;

    @Inject
    UserManager userManager;

    @Inject
    AnalyticsManager analyticsManager;

    @Inject
    NotificationFeedManager notificationFeedManager;

    private IDataSource<StoriesPreviewItemModel> dataSource;
    private PagingRecyclerViewBindingAdapter<StoriesPreviewItemModel> adapter;

    private boolean fromSearch;
    private SearchQuery searchQuery;

    private String avatarUrl;
    private int unreadNotifications;
    private boolean emptyState = false;

    public StoriesPreviewViewModel(StoriesPreviewActivity activity, boolean fromSearch, SearchQuery searchQuery) {
        super(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        this.fromSearch = fromSearch;
        this.searchQuery = searchQuery;

        if (fromSearch) {
            setTitle(getActivity().getString(R.string.all_stories));
            setNavIcon(R.drawable.ic_navigation_arrow_back_white);
        }
        else {
            setTitle(getActivity().getString(R.string.stories));
            setNavIcon(R.drawable.ic_menu);
            analyticsManager.trackScreen("Stories");
        }

        if (sessionManager.isLoggedIn()) {
            String userId = sessionManager.getCurrentSession().getUserId();
            userManager.getUser(userId).onErrorReturn(throwable -> null).subscribe(user -> {
                if (user != null) {
                    avatarUrl = user.getAvatar();
                    notifyPropertyChanged(BR.avatarUrl);
                }
            });
        }

        notifyPropertyChanged(BR.fromSearch);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isLoggedIn()) {
            notificationSubscription = notificationFeedManager.pollUnseenNotificationsCount()
                                                              .observeOn(AndroidSchedulers.mainThread())
                                                              .onErrorReturn(throwable -> null)
                                                              .subscribe(integer -> {
                                                                  if (integer == null) {
                                                                      return;
                                                                  }
                                                                  setUnreadNotifications(integer);
                                                                  notifyPropertyChanged(BR.unreadNotifications);
                                                              });
        }
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }
        super.onBound();
    }

    @Override
    public void refreshHeader(Session session) {
        super.refreshHeader(session);
        refreshNotifications();
    }

    private void refreshNotifications() {
        if (isLoggedIn()) {
            notificationFeedManager.getUnseenNotificationsCount()
                                   .observeOn(AndroidSchedulers.mainThread())
                                   .onErrorReturn(throwable -> null)
                                   .subscribe(integer -> {
                                       if (integer != null) {
                                           setUnreadNotifications(integer);
                                           notifyPropertyChanged(BR.unreadNotifications);
                                       }
                                   });
        }
    }

    @Bindable
    public boolean isFromSearch() {
        return (fromSearch);
    }

    public Action1<RecyclerView> onRecyclerViewCreated = recyclerView -> {
        if (fromSearch) {
            dataSource = new StoriesPreviewViewItemModelDataSource(getActivity(), searchManager.getStoryListDataSource());
        }
        else {
            dataSource = new StoriesPreviewViewItemModelDataSource(getActivity(), storyManager.getAllStories());
        }
        adapter = new PagingRecyclerViewBindingAdapter<>(R.layout.item_stories_preview, dataSource);
        adapter.getNewPageObservable()
               .onErrorReturn(throwable -> null)
               .subscribe(last -> {
                   if (last == null) {
                       if (fromSearch) {
                           searchStories(searchQuery, PAGE_SIZE, null);
                       }
                       else {
                           recentStories(PAGE_SIZE, null);
                       }
                   }
                   else {
                       if (fromSearch) {
                           searchStories(searchQuery, PAGE_SIZE, last.getId());
                       }
                       else {
                           recentStories(PAGE_SIZE, last.getId());
                       }
                   }
               });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    };

    private void recentStories(int pageSize, String lastId) {
        storyManager.recent(pageSize, lastId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorReturn(throwable -> {
                        SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_stories);
                        return null;
                    })
                    .subscribe(stories -> {
                        if (lastId == null) {
                            adapter.notifyDataSetChanged();
                            setEmptyState(adapter.getItemCount() == 0 && (stories == null || stories.isEmpty()));
                        }
                        else if (stories != null) {
                            adapter.notifyItemRangeChanged(adapter.getItemCount() - stories.size(), stories.size());
                        }
                        if (stories != null) {
                            analyticsManager.storiesScrolledBy(stories.size());
                        }
                    });
    }

    private void searchStories(SearchQuery searchQuery, int pageSize, String lastId) {
        searchManager.downloadStories(searchQuery, pageSize, lastId)
                     .subscribeOn(Schedulers.io())
                     .observeOn(AndroidSchedulers.mainThread())
                     .onErrorReturn(throwable -> {
                         SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_stories);
                         return null;
                     })
                     .subscribe(stories -> {
                         if (lastId == null) {
                             adapter.notifyDataSetChanged();
                             setEmptyState(adapter.getItemCount() == 0 && (stories == null || stories.isEmpty()));
                         }
                         else if (stories != null) {
                             adapter.notifyItemRangeChanged(adapter.getItemCount() - stories.size(), stories.size());
                         }
                     });
    }

    public Action1<SwipeRefreshLayout> refresh = swipeRefreshLayout -> {
        List<StoriesPreviewItemModel> list = new ArrayList<>();
        if (dataSource != null) {
            list.addAll(dataSource.list());
        }
        storyManager.clearStories();
        adapter.resetLastPagingPosition();
        if (fromSearch) {
            searchManager.downloadStories(searchQuery, PAGE_SIZE)
                         .observeOn(AndroidSchedulers.mainThread())
                         .subscribeOn(Schedulers.io())
                         .onErrorReturn(throwable -> {
                             setEmptyState(true);
                             SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_stories);
                             return null;
                         })
                         .subscribe(stories -> {
                             if (swipeRefreshLayout != null) {
                                 swipeRefreshLayout.setRefreshing(false);
                             }
                             adapter.refresh(list, stories, PAGE_SIZE);
                             setEmptyState(adapter.getItemCount() == 0 && (stories == null || stories.isEmpty()));
                         });
        }
        else {
            storyManager.recent(PAGE_SIZE)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .onErrorReturn(throwable -> {
                            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_stories);
                            return null;
                        })
                        .subscribe(stories -> {
                            if (swipeRefreshLayout != null) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            adapter.refresh(list, stories, PAGE_SIZE);
                            if (stories != null) {
                                analyticsManager.storiesScrolledBy(stories.size());
                            }
                        });
        }
    };

    @Bindable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String url) {
        avatarUrl = url;
        notifyPropertyChanged(BR.avatarUrl);
    }

    @Bindable
    public int getUnreadNotifications() {
        return unreadNotifications;
    }

    public void setUnreadNotifications(int unreadNotifications) {
        this.unreadNotifications = unreadNotifications;
    }

    @Bindable
    public boolean getEmptyState() {
        return emptyState;
    }

    public void setEmptyState(boolean emptyState) {
        this.emptyState = emptyState;
        notifyPropertyChanged(BR.emptyState);
    }

    @Bindable
    public boolean isLoggedIn() {
        return super.isLoggedIn();
    }

    public Action1<View> profilePictureClicked = view -> {
        NotificationFeedActivity.start(getActivity());
    };

    @Override
    public void onNotificationReceived() {
        refreshNotifications();
    }
}

package com.fresconews.fresco.v2.notificationfeed;

import android.databinding.Bindable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.NotificationFeedItemViewModelDataSource;
import com.fresconews.fresco.framework.mvvm.viewmodels.NotificationFeedItemViewModel;
import com.fresconews.fresco.framework.persistence.managers.NotificationFeedManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.models.Notification;
import com.fresconews.fresco.framework.recyclerview.PagingRecyclerViewBindingAdapter;
import com.fresconews.fresco.v2.profile.ProfileActivity;
import com.fresconews.fresco.v2.utils.SnackbarUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Maurice on 30/08/2016.
 */
public class NotificationFeedViewModel extends ActivityViewModel<NotificationFeedActivity> {
    private static final int PAGE_SIZE = 10;

    @Inject
    SessionManager sessionManager;

    @Inject
    NotificationFeedManager notificationFeedManager;

    private IDataSource<NotificationFeedItemViewModel> dataSource;
    private PagingRecyclerViewBindingAdapter<NotificationFeedItemViewModel> adapter;
    private boolean emptyState = false;

    public NotificationFeedViewModel(NotificationFeedActivity activity) {
        super(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent()
                                             .inject(this);

        setTitle(activity.getString(R.string.activity));
        setNavIcon(R.drawable.ic_close_black);

        notificationFeedManager.clearNotifications();
    }

    public Action1<RecyclerView> onRecyclerViewCreated = recyclerView -> {
        dataSource = new NotificationFeedItemViewModelDataSource(getActivity(),
                notificationFeedManager.getNotificationFeedDataSource(cursorList -> seeNotifications(cursorList.getAll())));

        adapter = new PagingRecyclerViewBindingAdapter<>(R.layout.item_notification_feed, dataSource);
        adapter.getNewPageObservable().onErrorReturn(throwable -> null).subscribe(last -> {
            if (last == null) {
                notificationFeedManager.downloadNotifications(PAGE_SIZE)
                                       .observeOn(AndroidSchedulers.mainThread())
                                       .subscribeOn(Schedulers.io())
                                       .onErrorReturn(throwable -> {
                                           setEmptyState(true);
                                           SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_activity_feed);
                                           return null;
                                       })
                                       .subscribe(notifications -> {
                                           adapter.notifyDataSetChanged();
                                           setEmptyState(adapter.getItemCount() == 0 && (notifications == null || notifications.isEmpty()));
                                           if (adapter.getItemCount() > 0) {
                                               seeNotifications(notifications);
                                           }
                                       });
            }
            else {
                notificationFeedManager.downloadNotifications(PAGE_SIZE, last.getId())
                                       .observeOn(AndroidSchedulers.mainThread())
                                       .subscribeOn(Schedulers.io())
                                       .onErrorReturn(throwable -> {
                                           SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_activity_feed);
                                           return null;
                                       })
                                       .filter(notifications -> notifications != null)
                                       .subscribe(notifications -> {
                                           if (adapter.getItemCount() > 0) {
                                               adapter.notifyItemRangeChanged(dataSource.getItemCount() - notifications.size(), notifications.size());
                                               seeNotifications(notifications);
                                           }
                                       });
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    };

    public Action1<SwipeRefreshLayout> refresh = swipeRefreshLayout -> {
        List<NotificationFeedItemViewModel> list = dataSource.list();
        notificationFeedManager.clearNotifications();
        adapter.resetLastPagingPosition();

        notificationFeedManager.downloadNotifications(PAGE_SIZE)
                               .observeOn(AndroidSchedulers.mainThread())
                               .subscribeOn(Schedulers.io())
                               .onErrorReturn(throwable -> {
                                   setEmptyState(true);
                                   SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_activity_feed);
                                   return null;
                               })
                               .subscribe(notifications -> {
                                   adapter.refresh(list, notifications, PAGE_SIZE);
                                   swipeRefreshLayout.setRefreshing(false);
                                   setEmptyState(adapter.getItemCount() == 0 && (notifications == null || notifications.isEmpty()));
                                   seeNotifications(notifications);
                               });
    };

    private void seeNotifications(List<Notification> notifications) {
        if (notifications != null) {
            List<String> ids = new ArrayList<>();
            for (Notification notification : notifications) {
                if (!notification.isSeen() && !TextUtils.isEmpty(notification.getId())) {
                    ids.add(notification.getId());
                }
            }
            if (!ids.isEmpty()) {
                notificationFeedManager.seeNotifications(ids)
                                       .onErrorReturn(throwable -> null)
                                       .subscribe();
            }
        }
    }

    public Action1<View> goToProfile = view -> {
        if (isLoggedIn()) {
            ProfileActivity.start(getActivity(), sessionManager.getCurrentSession()
                                                               .getUserId());
        }
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

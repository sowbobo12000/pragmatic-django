package com.fresconews.fresco.framework.mvvm.datasources;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.framework.mvvm.viewmodels.NotificationFeedItemViewModel;
import com.fresconews.fresco.framework.persistence.models.Notification;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maurice on 31/08/2016.
 */
public class NotificationFeedItemViewModelDataSource implements IDataSource<NotificationFeedItemViewModel> {
    private IDataSource<Notification> dataSource;
    private WeakReference<Activity> activity;

    public NotificationFeedItemViewModelDataSource(Activity paramActivity, IDataSource<Notification> paramDataSource) {
        dataSource = paramDataSource;
        activity = new WeakReference<>(paramActivity);
    }

    @Override
    public int getItemCount() throws IllegalStateException {
        return dataSource.getItemCount();
    }

    @Override
    public NotificationFeedItemViewModel get(int position) {
        return new NotificationFeedItemViewModel(activity.get(), dataSource.get(position));
    }

    @Override
    public void onBind(RecyclerView.Adapter adapter, int position) {
        if (dataSource != null) {
            dataSource.onBind(adapter, position);
        }
    }

    @Override
    public void onDetached() {
        if (dataSource != null) {
            dataSource.onDetached();
        }
    }

    @Override
    public List<NotificationFeedItemViewModel> list() {
        List<NotificationFeedItemViewModel> viewModels = new ArrayList<>();
        if (dataSource != null) {
            for (int i = 0; i < dataSource.getItemCount(); i++) {
                viewModels.add(get(i));
            }
        }
        return viewModels;
    }
}

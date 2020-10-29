package com.fresconews.fresco.framework.mvvm.datasources;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.framework.mvvm.viewmodels.UserAutoCompleteViewModel;
import com.fresconews.fresco.framework.persistence.models.User;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mauricewu on 11/11/16.
 */
public class UserAutoCompleteViewModelDataSource implements IDataSource<UserAutoCompleteViewModel> {
    private IDataSource<User> dataSource;
    private WeakReference<Activity> activity;
    private UserAutoCompleteViewModel.OnUserSelectListener onUserSelectListener;

    public UserAutoCompleteViewModelDataSource(Activity paramActivity, IDataSource<User> paramDataSource, UserAutoCompleteViewModel.OnUserSelectListener onUserSelectListener) {
        dataSource = paramDataSource;
        activity = new WeakReference<>(paramActivity);
        this.onUserSelectListener = onUserSelectListener;
    }

    @Override
    public int getItemCount() {
        return dataSource.getItemCount();
    }

    @Override
    public UserAutoCompleteViewModel get(int position) {
        if (activity != null) {
            return new UserAutoCompleteViewModel(activity.get(), dataSource.get(position), onUserSelectListener);
        }
        return null;
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
    public List<UserAutoCompleteViewModel> list() {
        List<UserAutoCompleteViewModel> viewModels = new ArrayList<>();
        if (dataSource != null) {
            for (int i = 0; i < dataSource.getItemCount(); i++) {
                viewModels.add(get(i));
            }
        }
        return viewModels;
    }
}

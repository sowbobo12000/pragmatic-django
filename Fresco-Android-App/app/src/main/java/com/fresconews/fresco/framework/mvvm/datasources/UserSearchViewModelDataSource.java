package com.fresconews.fresco.framework.mvvm.datasources;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.framework.mvvm.viewmodels.UserSearchViewModel;
import com.fresconews.fresco.framework.persistence.models.User;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Blaze on 8/4/2016.
 */
public class UserSearchViewModelDataSource implements IDataSource<UserSearchViewModel> {
    private IDataSource<User> dataSource;
    private WeakReference<Activity> activity;
    private WeakReference<Fragment> fragment;

    public UserSearchViewModelDataSource(Activity paramActivity, IDataSource<User> paramDataSource) {
        dataSource = paramDataSource;
        activity = new WeakReference<>(paramActivity);
    }

    public UserSearchViewModelDataSource(Fragment paramFragment, IDataSource<User> paramDataSource) {
        dataSource = paramDataSource;
        fragment = new WeakReference<>(paramFragment);
    }

    @Override
    public int getItemCount() {
        return dataSource.getItemCount();
    }

    @Override
    public UserSearchViewModel get(int position) {
        if (activity != null) {
            return new UserSearchViewModel(activity.get(), dataSource.get(position));
        }
        return new UserSearchViewModel(fragment.get(), dataSource.get(position));
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
    public List<UserSearchViewModel> list() {
        List<UserSearchViewModel> viewModels = new ArrayList<>();
        if (dataSource != null) {
            for (int i = 0; i < dataSource.getItemCount(); i++) {
                viewModels.add(get(i));
            }
        }
        return viewModels;
    }
}

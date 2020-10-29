package com.fresconews.fresco.framework.mvvm.datasources;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.framework.mvvm.viewmodels.CommentViewModel;
import com.fresconews.fresco.framework.persistence.models.Comment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Blaze on 8/26/2016.
 */
public class CommentViewModelDataSource implements IDataSource<CommentViewModel> {
    private IDataSource<Comment> dataSource;
    private WeakReference<Activity> activity;

    public CommentViewModelDataSource(Activity activity, IDataSource<Comment> dataSource) {
        this.dataSource = dataSource;
        this.activity = new WeakReference<>(activity);
    }

    @Override
    public int getItemCount() {
        return dataSource.getItemCount();
    }

    @Override
    public CommentViewModel get(int position) {
        return new CommentViewModel(activity.get(), dataSource.get(position));
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
    public List<CommentViewModel> list() {
        List<CommentViewModel> viewModels = new ArrayList<>();
        if (dataSource != null) {
            for (int i = 0; i < dataSource.getItemCount(); i++) {
                viewModels.add(get(i));
            }
        }
        return viewModels;
    }
}

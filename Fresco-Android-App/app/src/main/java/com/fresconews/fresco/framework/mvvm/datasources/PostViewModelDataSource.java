package com.fresconews.fresco.framework.mvvm.datasources;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.framework.mvvm.viewmodels.PostViewModel;
import com.fresconews.fresco.framework.persistence.models.Post;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wumau on 12/14/2016.
 */
public class PostViewModelDataSource implements IDataSource<PostViewModel> {
    private IDataSource<Post> dataSource;
    private WeakReference<Activity> activity;

    public PostViewModelDataSource(Activity activity, IDataSource<Post> dataSource) {
        this.dataSource = dataSource;
        this.activity = new WeakReference<>(activity);
    }

    @Override
    public int getItemCount() {
        if(dataSource == null){
            return 0;
        }
        return dataSource.getItemCount();
    }

    @Override
    public PostViewModel get(int position) {
        return new PostViewModel(activity.get(), dataSource.get(position));
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
    public List<PostViewModel> list() {
        List<PostViewModel> viewModels = new ArrayList<>();
        if (dataSource != null) {
            for (int i = 0; i < dataSource.getItemCount(); i++) {
                viewModels.add(get(i));
            }
        }
        return viewModels;
    }
}

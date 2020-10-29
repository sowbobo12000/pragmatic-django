package com.fresconews.fresco.framework.mvvm.datasources;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.framework.mvvm.viewmodels.StoriesPreviewItemModel;
import com.fresconews.fresco.framework.persistence.models.Story;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by techjini on 5/7/16.
 */
public class StoriesPreviewViewItemModelDataSource implements IDataSource<StoriesPreviewItemModel> {
    private IDataSource<Story> dataSource;
    private WeakReference<Activity> activity;

    public StoriesPreviewViewItemModelDataSource(Activity paramActivity, IDataSource<Story> paramDataSource) {
        dataSource = paramDataSource;
        activity = new WeakReference<>(paramActivity);
    }

    @Override
    public int getItemCount() {
        return dataSource.getItemCount();
    }

    @Override
    public StoriesPreviewItemModel get(int position) {
        return new StoriesPreviewItemModel(activity.get(), dataSource.get(position));
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
    public List<StoriesPreviewItemModel> list() {
        List<StoriesPreviewItemModel> viewModels = new ArrayList<>();
        if (dataSource != null) {
            for (int i = 0; i < dataSource.getItemCount(); i++) {
                viewModels.add(get(i));
            }
        }
        return viewModels;
    }
}

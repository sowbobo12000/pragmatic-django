package com.fresconews.fresco.framework.mvvm.datasources;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.framework.mvvm.viewmodels.GalleryViewModel;
import com.fresconews.fresco.framework.persistence.models.Gallery;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan on 6/10/2016.
 */
public class GalleryViewModelDataSource implements IDataSource<GalleryViewModel> {
    private IDataSource<Gallery> dataSource;
    private WeakReference<Activity> activity;

    public GalleryViewModelDataSource(Activity activity, IDataSource<Gallery> dataSource) {
        this.dataSource = dataSource;
        this.activity = new WeakReference<>(activity);
    }

    @Override
    public int getItemCount() {
        return dataSource.getItemCount();
    }

    @Override
    public GalleryViewModel get(int position) {
        return new GalleryViewModel(activity.get(), dataSource.get(position));
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
    public List<GalleryViewModel> list() {
        List<GalleryViewModel> viewModels = new ArrayList<>();
        if (dataSource != null) {
            for (int i = 0; i < dataSource.getItemCount(); i++) {
                viewModels.add(get(i));
            }
        }
        return viewModels;
    }
}

package com.fresconews.fresco.framework.mvvm.datasources;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.framework.mvvm.ItemViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.GalleryViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.StoriesPreviewItemModel;
import com.fresconews.fresco.framework.persistence.models.FeedRecord;
import com.fresconews.fresco.framework.persistence.models.Gallery;
import com.fresconews.fresco.framework.persistence.models.Gallery_Table;
import com.fresconews.fresco.framework.persistence.models.Story;
import com.fresconews.fresco.framework.persistence.models.Story_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryan on 8/10/2016.
 */
public class MixedFeedDataSource implements IDataSource<ItemViewModel> {
    private IDataSource<? extends BaseModel> dataSource;
    private WeakReference<Activity> activity;
    private boolean showReposted = false;

    public MixedFeedDataSource(Activity activity, IDataSource<? extends BaseModel> dataSource) {
        this.activity = new WeakReference<>(activity);
        this.dataSource = dataSource;
    }

    public MixedFeedDataSource(Activity activity, IDataSource<? extends BaseModel> dataSource, boolean showReposted) {
        this.activity = new WeakReference<>(activity);
        this.dataSource = dataSource;
        this.showReposted = showReposted;
    }

    @Override
    public int getItemCount() {
        return dataSource.getItemCount();
    }

    @Override
    public ItemViewModel get(int position) {
        FeedRecord record = (FeedRecord) dataSource.get(position);
        if (record.getType().equals("story")) {
            Story story = SQLite.select()
                                .from(Story.class)
                                .where(Story_Table.id.eq(record.getItemId()))
                                .querySingle();
            return new StoriesPreviewItemModel(activity.get(), story, showReposted);
        }
        else {
            Gallery gallery = SQLite.select()
                                    .from(Gallery.class)
                                    .where(Gallery_Table.id.eq(record.getItemId()))
                                    .querySingle();
            return new GalleryViewModel(activity.get(), gallery, showReposted);
        }
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
    public List<ItemViewModel> list() {
        List<ItemViewModel> viewModels = new ArrayList<>();
        if (dataSource != null) {
            for (int i = 0; i < dataSource.getItemCount(); i++) {
                viewModels.add(get(i));
            }
        }
        return viewModels;
    }
}

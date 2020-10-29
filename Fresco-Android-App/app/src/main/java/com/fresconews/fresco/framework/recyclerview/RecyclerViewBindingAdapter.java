package com.fresconews.fresco.framework.recyclerview;

import android.graphics.Point;
import android.support.annotation.LayoutRes;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.fresconews.fresco.framework.mvvm.ItemViewModel;
import com.fresconews.fresco.framework.mvvm.ViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.mvvm.viewmodels.GalleryViewModel;
import com.fresconews.fresco.framework.persistence.models.Gallery_Post;
import com.fresconews.fresco.framework.persistence.models.Gallery_Post_Table;
import com.fresconews.fresco.framework.persistence.models.Post;
import com.fresconews.fresco.framework.persistence.models.Post_Table;
import com.fresconews.fresco.v2.utils.ResizeUtils;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

/**
 * Created by Ryan on 5/31/2016.
 */
public class RecyclerViewBindingAdapter<T extends ViewModel> extends RecyclerView.Adapter<BindingViewHolder<T>> {
    @LayoutRes
    private int itemLayout;

    protected IDataSource<T> dataSource;

    public RecyclerViewBindingAdapter() {
    }

    public RecyclerViewBindingAdapter(@LayoutRes int itemLayout, IDataSource<T> dataSource) {
        this.itemLayout = itemLayout;
        setDataSource(dataSource);
    }

    @Override
    public BindingViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new BindingViewHolder<>(view);
    }

    @Override
    public void onBindViewHolder(BindingViewHolder<T> holder, int position) {
        if (dataSource != null && dataSource.get(position) != null) {
            holder.bind(dataSource.get(position));

            if (dataSource.get(position) != null && dataSource.get(position) instanceof GalleryViewModel) {
                GalleryViewModel galleryViewModel = (GalleryViewModel) dataSource.get(position);
                String galleryId = galleryViewModel.getId();
                if (galleryId != null) {
                    Point size = ResizeUtils.calculateAndSetViewPagerHeight(getPostsForGallery(galleryId));
                    ViewGroup viewGroup = (ViewGroup) holder.itemView;
                    resizePostsViewPager(viewGroup, size);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (dataSource == null) {
            return 0;
        }
        return dataSource.getItemCount();
    }

    public IDataSource<T> getDataSource() {
        return dataSource;
    }

    public void setDataSource(IDataSource<T> dataSource) {
        this.dataSource = dataSource;
        notifyDataSetChanged();
    }

    private void resizePostsViewPager(View viewGroup, Point size) {
        if (viewGroup instanceof ViewPager) {
            if (size != null) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) viewGroup.getLayoutParams();
                params.width = size.x;
                params.height = size.y;
                viewGroup.setLayoutParams(params);
                viewGroup.requestLayout();
                return;
            }
        }

        if (viewGroup instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) viewGroup).getChildCount(); i++) {
                View view = ((ViewGroup) viewGroup).getChildAt(i);
                resizePostsViewPager(view, size);
            }
        }
    }

    public void refresh(List<? extends ItemViewModel> oldList, List<? extends BaseModel> results, int pageSize) {
        refresh(oldList, results, pageSize, false);
    }

    public void refresh(List<? extends ItemViewModel> oldList, List<? extends BaseModel> results, int pageSize, boolean forceRefresh) {
        if (forceRefresh) {
            if (oldList == null || results == null) {
                notifyDataSetChanged();
            }
            else {
                boolean sameList = true;
                for (int i = 0; i < Math.min(oldList.size(), results.size()); i++) {
                    if (oldList.get(i) != null && oldList.get(i).getItem() != null && results.get(i) != null && !oldList.get(i).getItem().equals(results.get(i))) {
                        sameList = false;
                    }
                }
                if (!sameList) {
                    notifyDataSetChanged();
                }
            }
        }
        else {
            if (oldList != null && results != null) {
                int min = Math.min(oldList.size(), results.size());
                for (int i = 0; i < min; i++) {
                    if (oldList.get(i) != null && oldList.get(i).getItem() != null && results.get(i) != null && !oldList.get(i).getItem().equals(results.get(i))) {
                        notifyItemChanged(i);
                    }
                }
                if (min < pageSize) {
                    notifyItemRangeChanged(min, pageSize - min);
                }
            }
            else {
                notifyItemRangeChanged(0, pageSize);
            }
            if (oldList != null && oldList.size() > pageSize) {
                notifyItemRangeRemoved(pageSize, oldList.size());
            }
        }
    }

    private List<Post> getPostsForGallery(String galleryId) {
        return SQLite.select()
                     .from(Post.class)
                     .innerJoin(Gallery_Post.class)
                     .on(Post_Table.id.withTable().eq(Gallery_Post_Table.post_id.withTable()))
                     .where(Gallery_Post_Table.gallery_id.withTable().eq(galleryId))
                     .groupBy(Gallery_Post_Table.post_id.withTable())
//                     .orderBy(Post_Table.createdAt, false)
                     .orderBy(Post_Table.index, true)
                     .queryList();
    }
}

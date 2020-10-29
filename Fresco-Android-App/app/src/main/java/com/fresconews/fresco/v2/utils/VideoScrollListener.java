package com.fresconews.fresco.v2.utils;

import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.mvvm.ViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.GalleryViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;

/**
 * Created by ryan on 8/12/2016.
 */
public class VideoScrollListener<T extends ViewModel> extends RecyclerView.OnScrollListener {
    private static final String TAG = VideoScrollListener.class.getSimpleName();

    private IDataSource<T> mDataSource;

    public VideoScrollListener(IDataSource<T> dataSource) {
        mDataSource = dataSource;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) { // Stopped Scrolling
            setActivePost(recyclerView);
        }
    }

    private void setActivePost(RecyclerView recyclerView) {
        // Gets the first view that's fully visible. If none are fully visible, then get
        // the one that has the most area on screen

        LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();

        int position = manager.findFirstCompletelyVisibleItemPosition();
        if (position == -1) {
            position = manager.findFirstVisibleItemPosition();

            if (position + 1 < mDataSource.getItemCount() && manager.findViewByPosition(position + 1) != null) {
                //Get the actual content region
                View v1 = manager.findViewByPosition(position).findViewById(R.id.viewpager);
                View v2 = manager.findViewByPosition(position + 1).findViewById(R.id.viewpager);

                if (v1 == null || v2 == null) { //Sanity check
                    Fresco2.setActivePostIdInViewAnalytics("stop", null, true);

                    return;
                }

                Rect rect1 = new Rect();
                boolean v1Visible = v1.getGlobalVisibleRect(rect1);

                Rect rect2 = new Rect();
                boolean v2Visible = v2.getGlobalVisibleRect(rect2);

                if (!v1Visible && !v2Visible) { //Sanity check, a view pager should always be in view
                    Fresco2.setActivePostIdInViewAnalytics("stop", null, true);

                    return;
                }
                else if (!v1Visible || rect1.width() * rect1.height() < rect2.width() * rect2.height()) {
                    position += 1;
                }
            }
        }
        if (position < 0 || position >= mDataSource.getItemCount()) {
            Fresco2.setActivePostIdInViewAnalytics("stop", null, true);
        }
        else {
            ViewModel model = mDataSource.get(position);
            if (model instanceof GalleryViewModel) {
                Object tag = manager.findViewByPosition(position).getTag();
                if (tag != null) {
                    String postId = tag.toString();
                    Fresco2.setActivePostIdInViewAnalytics(postId, null, true);
                }
            }
            else {
                Fresco2.setActivePostIdInViewAnalytics("stop", null, true);
            }
        }
    }
}

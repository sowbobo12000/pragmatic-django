package com.fresconews.fresco.v2.mediabrowser;

import android.content.Intent;
import android.graphics.Typeface;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.recyclerview.BindingViewHolder;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.recyclerview.RecyclerViewBindingAdapter;
import com.fresconews.fresco.v2.utils.LogUtils;

public class MediaBrowserRecyclerViewAdapter extends RecyclerViewBindingAdapter<MediaItemViewModel> {
    private static final int ITEM_TYPE_FOOTER = 0;
    private static final int ITEM_TYPE_ITEM = 1;

    public MediaBrowserRecyclerViewAdapter(IDataSource<MediaItemViewModel> dataSource) {
        super(R.layout.media_item_view, dataSource);
    }

    @Override
    public int getItemViewType(int position) {
        if (isFooter(position)) {
            return ITEM_TYPE_FOOTER;
        }
        else {
            return ITEM_TYPE_ITEM;
        }
    }

    @Override
    public BindingViewHolder<MediaItemViewModel> onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_FOOTER) {
            View footer = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_browser_bot_loc_message, parent, false);

            return new BindingViewHolder<>(footer);
        }
        else {
            return super.onCreateViewHolder(parent, viewType);
        }
    }

    public boolean isFooter(int position) {
        return position == getItemCount() - 1;
    }


}

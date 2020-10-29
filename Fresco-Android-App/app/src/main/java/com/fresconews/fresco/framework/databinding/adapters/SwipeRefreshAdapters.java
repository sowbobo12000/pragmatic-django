package com.fresconews.fresco.framework.databinding.adapters;

import android.databinding.BindingAdapter;
import android.support.v4.widget.SwipeRefreshLayout;

import rx.functions.Action1;

/**
 * Created by ryan on 6/28/2016.
 */
public class SwipeRefreshAdapters {
    @BindingAdapter({"refresh"})
    public static void refresh(SwipeRefreshLayout swipeRefreshLayout, Action1<SwipeRefreshLayout> action) {
        swipeRefreshLayout.setOnRefreshListener(() -> action.call(swipeRefreshLayout));
    }
}

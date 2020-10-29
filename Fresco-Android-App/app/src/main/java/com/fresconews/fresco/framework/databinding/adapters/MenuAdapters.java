package com.fresconews.fresco.framework.databinding.adapters;

import android.databinding.BindingAdapter;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import rx.functions.Func1;

public class MenuAdapters {
    @BindingAdapter({"menu"})
    public static void bindMenu(Toolbar toolbar, int menuId) {
        toolbar.inflateMenu(menuId);
    }

    @BindingAdapter({"onMenuItemClick"})
    public static void bindMenuOnClick(Toolbar toolbar, Func1<MenuItem, Boolean> function) {
        toolbar.setOnMenuItemClickListener(function::call);
    }
}

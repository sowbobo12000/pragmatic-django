package com.fresconews.fresco.framework.databinding.adapters;

import android.databinding.BindingAdapter;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import rx.functions.Action1;
import rx.functions.Func1;

public class NavigationAdapters {

    @BindingAdapter({"onNavIconClick"})
    public static void onNavIconClicked(Toolbar toolbar, Action1<View> action) {
        toolbar.setNavigationOnClickListener(action::call);
    }

    @BindingAdapter({"onNavItemSelected"})
    public static void onNavItemSelected(NavigationView navigationView, Func1<MenuItem, Boolean> action) {
        navigationView.setNavigationItemSelectedListener(action::call);
    }
}

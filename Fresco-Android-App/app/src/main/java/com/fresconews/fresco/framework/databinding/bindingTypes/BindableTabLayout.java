package com.fresconews.fresco.framework.databinding.bindingTypes;

import android.support.design.widget.TabLayout;

public class BindableTabLayout extends BaseObservable {
    TabLayout tabLayout;

    public TabLayout get() {
        return tabLayout;
    }

    public void set(TabLayout value) {
        if (tabLayout != value) {
            this.tabLayout = value;
            notifyChange();
        }
    }
}
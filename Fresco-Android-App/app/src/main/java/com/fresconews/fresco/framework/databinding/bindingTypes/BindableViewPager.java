package com.fresconews.fresco.framework.databinding.bindingTypes;

import android.support.v4.view.ViewPager;

public class BindableViewPager extends BaseObservable {
    ViewPager mViewPager;

    public ViewPager get() {
        return mViewPager;
    }

    public void set(ViewPager value) {
        if (mViewPager != value) {
            this.mViewPager = value;
            notifyChange();
        }
    }
}
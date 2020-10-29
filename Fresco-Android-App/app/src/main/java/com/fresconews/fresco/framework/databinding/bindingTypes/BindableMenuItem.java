package com.fresconews.fresco.framework.databinding.bindingTypes;

import android.view.MenuItem;

public class BindableMenuItem extends BaseObservable {
    MenuItem mMenuItem;

    public MenuItem get() {
        return mMenuItem;
    }

    public void set(MenuItem value) {
        if (mMenuItem != value) {
            this.mMenuItem = value;
            notifyChange();
        }
    }
}
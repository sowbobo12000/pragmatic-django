package com.fresconews.fresco.framework.databinding.bindingTypes;

import android.view.Menu;

public class BindableMenu extends BaseObservable {
    Menu mMenu;

    public Menu get() {
        return mMenu;
    }

    public void set(Menu value) {
        if (mMenu != value) {
            this.mMenu = value;
            notifyChange();
        }
    }
}
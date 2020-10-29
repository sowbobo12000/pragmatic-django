package com.fresconews.fresco.framework.databinding.bindingTypes;

import android.support.v7.widget.RecyclerView;

public class BindableRecyclerView extends BaseObservable {
    RecyclerView recyclerView;

    public RecyclerView get() {
        return recyclerView;
    }

    public void set(RecyclerView value) {
        if (recyclerView != value) {
            this.recyclerView = value;
            notifyChange();
        }
    }

}
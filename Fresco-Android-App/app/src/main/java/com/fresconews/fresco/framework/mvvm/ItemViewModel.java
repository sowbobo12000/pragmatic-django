package com.fresconews.fresco.framework.mvvm;

import android.support.annotation.CallSuper;

public abstract class ItemViewModel<T> extends ViewModel {
    private T item;
    protected boolean hasBeenBound = false;

    public ItemViewModel(T item) {
        this.item = item;
    }

    public T getItem() {
        return item;
    }

    public void setItem(T mItem) {
        this.item = mItem;
    }

    @Override
    @CallSuper
    public void onBound() {
        if (!hasBeenBound) {
            hasBeenBound = true;
        }
    }
}

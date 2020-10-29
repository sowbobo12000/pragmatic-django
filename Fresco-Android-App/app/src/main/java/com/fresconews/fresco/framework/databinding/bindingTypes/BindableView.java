package com.fresconews.fresco.framework.databinding.bindingTypes;

import android.view.View;

/**
 * Created by ryan on 6/15/2016.
 */
public class BindableView<T extends View> extends BaseObservable {
    private T mView;

    public T get() {
        return mView;
    }

    public void set(T view) {
        if (mView != view) {
            mView = view;
            notifyChange();
        }
    }
}

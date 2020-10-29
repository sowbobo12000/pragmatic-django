package com.fresconews.fresco.framework.databinding.bindingTypes;

import com.fresconews.fresco.framework.databinding.bindingUtils.SwipePayload;

public class BindableSwipePayload extends BaseObservable {
    SwipePayload swipePayload;

    public SwipePayload get() {
        return swipePayload;
    }

    public void set(SwipePayload value) {
        if (swipePayload != value) {
            this.swipePayload = value;
            notifyChange();
        }
    }
}
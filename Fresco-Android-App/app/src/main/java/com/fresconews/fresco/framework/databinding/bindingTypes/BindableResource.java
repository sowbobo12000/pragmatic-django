package com.fresconews.fresco.framework.databinding.bindingTypes;

public class BindableResource extends BaseObservable {
    int mValue;

    public int get() {
        return mValue;
    }

    public void set(int value) {
        if (mValue != value) {
            this.mValue = value;
            notifyChange();
        }
    }
}
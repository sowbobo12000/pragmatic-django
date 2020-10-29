package com.fresconews.fresco.framework.databinding.bindingTypes;

public class BindableFloat extends BaseObservable {
    float mValue;

    public float get() {
        return mValue;
    }

    public void set(float value) {
        if (mValue != value) {
            this.mValue = value;
            notifyChange();
        }
    }
}
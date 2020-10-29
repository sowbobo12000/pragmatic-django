package com.fresconews.fresco.framework.databinding.bindingTypes;


public class BindableStringAlways extends BaseObservable {
    String value;

    public String get() {
        return value != null ? value : "";
    }

    public void set(String value) {
        this.value = value;
        notifyChange();
    }

    public boolean isEmpty() {
        return value == null || value.isEmpty();
    }
}
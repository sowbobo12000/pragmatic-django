package com.fresconews.fresco.framework.databinding.adapters;

import android.databinding.BindingAdapter;
import android.support.design.widget.TextInputLayout;

/**
 * Created by ryan on 6/21/2016.
 */
public class TextInputLayoutAdapters {
    @BindingAdapter({"error"})
    public static void bindTextInputLayoutError(TextInputLayout layout, String error) {
        if (error == null || error.equals("")) {
            layout.setErrorEnabled(false);
            layout.setError("");
        }
        else {
            layout.setErrorEnabled(true);
            layout.setError(error);
        }
    }
}

package com.fresconews.fresco.framework.databinding.bindingAdapters;

import android.content.res.AssetManager;
import android.databinding.BindingAdapter;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.TextView;

import com.fresconews.fresco.Fresco2;

/**
 * ActionBindingAdapters - this is where images, LinearLayout, fonts, image avatars, etc get set with attributes and displayed in the relevant view
 * For relevant views they can be set to visible, gone, etc...
 */
public class ActionBindingAdapters {

    @BindingAdapter({"customFont"})
    public static void customFont(TextView view, String customFont) {
        Typeface font = getTypeface(customFont);
        view.setTypeface(font);
    }

    @BindingAdapter("customFont")
    public static void customFont(Button button, String customFont) {
        Typeface font = getTypeface(customFont);
        button.setTypeface(font);
    }

    private static Typeface getTypeface(String customFont) {
        if (!Fresco2.fontCache.containsKey(customFont.hashCode())) {
            AssetManager assetManager = Fresco2.getContext().getAssets();
            Typeface tf = Typeface.createFromAsset(assetManager, customFont);
            Fresco2.fontCache.put(customFont.hashCode(), tf);
        }
        return Fresco2.fontCache.get(customFont.hashCode());
    }
}

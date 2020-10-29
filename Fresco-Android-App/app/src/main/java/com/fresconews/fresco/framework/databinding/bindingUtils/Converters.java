package com.fresconews.fresco.framework.databinding.bindingUtils;

import android.databinding.BindingConversion;

import com.fresconews.fresco.framework.databinding.bindingTypes.BindableBoolean;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableString;

public class Converters {
    @BindingConversion
    public static String convertBindableToString(BindableString bindableString) {
        if(bindableString == null){
            return null;
        }
        return bindableString.get();
    }

    @BindingConversion
    public static boolean convertBindableToBoolean(BindableBoolean bindableBoolean) {
        return bindableBoolean.get();
    }
}

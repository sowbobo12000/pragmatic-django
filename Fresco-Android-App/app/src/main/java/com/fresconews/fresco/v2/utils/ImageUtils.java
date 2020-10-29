package com.fresconews.fresco.v2.utils;

import android.support.annotation.StringDef;

/**
 * Created by ryan on 6/21/2016.
 */
public class ImageUtils {

    public static final String SMALL = "small";
    public static final String MEDIUM = "medium";
    public static final String LARGE = "large";

    @StringDef({SMALL, MEDIUM, LARGE})
    public @interface ImageSize {
    }

    public static String getImageSizeV1(String imageUrl, @ImageSize String imageSize) {
        if (imageUrl == null) {
            return null;
        }
        return imageUrl.replace("/images", "/images/" + imageSize);
    }

    public static String getImageSizeV2(String imageUrl, int width) {
        if (imageUrl == null) {
            return null;
        }
        return imageUrl.replace("/images", "/images/" + width);
    }
}

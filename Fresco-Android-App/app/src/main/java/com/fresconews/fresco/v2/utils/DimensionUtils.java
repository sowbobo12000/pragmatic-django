package com.fresconews.fresco.v2.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.fresconews.fresco.Fresco2;

/**
 * Created by wumau on 9/27/2016.
 */

public class DimensionUtils {
    private static Point screenDimensions;

    public static Point getScreenDimensions() {
        if (screenDimensions == null || screenDimensions.x == 0 || screenDimensions.y == 0) {
            Display display = ((WindowManager) Fresco2.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            screenDimensions = new Point();
            display.getSize(screenDimensions);
        }
        return screenDimensions;
    }

    /**
     * This method converts dp unit to equivalent device specific value in pixels.
     *
     * @param dp      A value in dp(Device independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return An integer value to represent Pixels equivalent to dp according to device
     */
    public static int convertDpToPixel(float dp, Context context) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        return  px / Resources.getSystem().getDisplayMetrics().density;
    }

    public static float convertPixelsToDp(float px) {
        return px / Resources.getSystem().getDisplayMetrics().density;
    }
}

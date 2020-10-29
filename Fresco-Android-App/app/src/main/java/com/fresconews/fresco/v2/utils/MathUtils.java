package com.fresconews.fresco.v2.utils;

/**
 * Created by wumau on 9/30/2016.
 */

public class MathUtils {
    public static float round(float value, int decimalPlaces) {
        float pow = (float) Math.pow(10, decimalPlaces);
        return ((int) (value * pow + .5f) / pow);
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static int clamp(int x, int min, int max) {
        return Math.max(Math.min(x, max), min);
    }

    public static int map(int x, int inMin, int inMax, int outMin, int outMax) {
        return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

    public static boolean approxEquals(int x, int target, double marginError) {
        return x * (1 - marginError) < target && x * (1 + marginError) > target;
    }
}

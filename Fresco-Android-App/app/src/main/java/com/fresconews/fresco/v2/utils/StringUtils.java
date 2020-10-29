package com.fresconews.fresco.v2.utils;

import android.util.Patterns;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wumau on 9/27/2016.
 */

public class StringUtils {
    public static String formatDistance(double distanceMiles) {
        if (distanceMiles < 5) {
            return String.format(Locale.getDefault(), "%.1f miles away", distanceMiles);
        }
        else {
            return String.format(Locale.getDefault(), "%.0f miles away", distanceMiles);
        }
    }

    public static String formatDistanceAway(int distanceFeet) {
        if (distanceFeet > 5280) {
            return formatDistance(distanceFeet / 5280.0);
        }
        return String.format(Locale.getDefault(), "%d ft away", distanceFeet);
    }

    public static String toNullIfEmpty(String str) {
        if (str == null) {
            return null;
        }
        if (str.trim().equals("")) {
            return null;
        }
        return str;
    }

    public static boolean isEmailValid(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean stringContainsWord(String fullString, String partWord) {
        //Log.i(TAG, "submission: contains word media type check -> " + fullString);
        String pattern = "\\b" + partWord + "\\b";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(fullString);
        return m.find();
    }

    /**
     * Strips a leading '@' from usernames (relevant for twitter signups)
     *
     * @param username
     * @return
     */
    public static String filterUserName(String username) {
        if (username.startsWith("@")) {
            return username.substring(1).trim();
        }
        return username.trim();
    }

    //Magical (trust me) (works up to the trillions)
    public static String abbrNum(double number, double decPlaces) {

        String abbreviatedNumber = Double.toString(number);

        if (number < 1000) {
            return Integer.toString((int) number);
        }

        // 2 decimal places => 100, 3 => 1000, etc
        decPlaces = Math.pow(10, decPlaces);

        // Enumerate number abbreviations
        String[] abbrev = {"k", "m", "b", "t"};

        // Go through the array backwards, so we do the largest first
        for (int i = abbrev.length - 1; i >= 0; i--) {

            // Convert array index to "1000", "1000000", etc
            double size = Math.pow(10, (i + 1) * 3);

            // If the number is bigger or equal do the abbreviation
            if (size <= number) {
                // Here, we multiply by decPlaces, round, and then divide by decPlaces.
                // This gives us nice rounding to a particular decimal place.
                number = Math.round(number * decPlaces / size) / decPlaces;

                // Handle special case where we round up to the next abbreviation
                if ((number == 1000) && (i < abbrev.length - 1)) {
                    number = 1;
                    i++;
                }

                // Add the letter for the abbreviation
                abbreviatedNumber = Double.toString(number) + abbrev[i];

                // We are done... stop
                break;
            }

        }
        //TODO I guess i could add a final check if the number ends in .0 cause that's lame.

        return abbreviatedNumber;
    }
}

package com.fresconews.fresco.framework.persistence.managers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ryan on 6/20/2016.
 */
public class LocalSettingsManager {
    private static final String TAG = LocalSettingsManager.class.getSimpleName();

    private static final String SETTING_PREFERENCE = "FRESCO_SETTINGS";
    private static final String SETTING_FIRSTRUN = "FRESCO_SETTINGS_FIRSTRUN";
    private static final String SETTING_PRINT_API_LOGS = "FRESCO_SETTING_PRINT_API_LOGS";

    private Context context;

    public LocalSettingsManager(Context context) {
        this.context = context;
    }

    public boolean isFirstRun() {
        SharedPreferences preferences = context.getSharedPreferences(SETTING_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getBoolean(SETTING_FIRSTRUN, true);
    }

    public void setFirstRun(boolean firstRun) {
        SharedPreferences preferences = context.getSharedPreferences(SETTING_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(SETTING_FIRSTRUN, firstRun);
        editor.apply();
    }

    public boolean printAPILogs() {
        SharedPreferences preferences = context.getSharedPreferences(SETTING_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getBoolean(SETTING_PRINT_API_LOGS, true);
    }

    public void setPrintAPILogs(boolean printAPILogs) {
        SharedPreferences preferences = context.getSharedPreferences(SETTING_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(SETTING_PRINT_API_LOGS, printAPILogs);
        editor.apply();
    }
}

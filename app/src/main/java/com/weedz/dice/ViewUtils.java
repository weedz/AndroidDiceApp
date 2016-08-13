package com.weedz.dice;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by WeeDz on 2016-08-11.
 */
public class ViewUtils {

    public static void ApplyTheme(Activity activity, SharedPreferences pref) {
        // Set default values for settings
        PreferenceManager.setDefaultValues(activity, R.xml.pref_main, false);
        if (pref.getBoolean("pref_settings_dark_theme", false)) {
            activity.setTheme(R.style.AppThemeDark);
        } else {
            activity.setTheme(R.style.AppTheme);
        }
    }
    public static void ApplyTheme(Activity activity) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        ApplyTheme(activity, pref);
    }

}

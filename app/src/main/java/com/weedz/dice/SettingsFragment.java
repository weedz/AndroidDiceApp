package com.weedz.dice;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by WeeDz on 2016-08-08.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_main);

        // set texts correctly
        onSharedPreferenceChanged(null, "");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_settings_dark_theme")) {
            // Apply dark theme
            getActivity().setTheme(R.style.AppThemeDark);
            getActivity().recreate();
        }
    }


}
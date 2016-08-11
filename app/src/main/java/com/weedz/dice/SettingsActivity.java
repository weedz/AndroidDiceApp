package com.weedz.dice;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
public class SettingsActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (pref.getBoolean("pref_settings_dark_theme", false)) {
            //setTheme(R.style.AppThemeDark);
            onApplyThemeResource(getTheme(), R.style.AppThemeDark, false);
        }

        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

}

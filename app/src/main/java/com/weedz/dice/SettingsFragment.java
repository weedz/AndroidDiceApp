package com.weedz.dice;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by WeeDz on 2016-08-08.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_main);
    }
}
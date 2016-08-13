package com.weedz.dice;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public interface OnResourceUpdatedListener {
        void onResourceUpdated();
    }

    private OnResourceUpdatedListener mListener;

    private static final String TAG = "SettingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_main);

        // set texts correctly
        onSharedPreferenceChanged(null, "");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnResourceUpdatedListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        mListener = (OnResourceUpdatedListener)getActivity();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_settings_dark_theme")) {
            getActivity().recreate();
            if (mListener != null) {
                mListener.onResourceUpdated();
            } else {
                Toast.makeText(getActivity(), "Restart app to apply theme", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
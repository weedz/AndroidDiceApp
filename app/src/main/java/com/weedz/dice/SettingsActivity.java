package com.weedz.dice;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.OnResourceUpdatedListener{

    private static final String TAG = "SettingsActivity";
    private int resourceState = 0;

    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        ViewUtils.ApplyTheme(this, pref);

        if (savedInstanceState != null) {
            resourceState = savedInstanceState.getInt("resourceState");
            setResult(resourceState);
        }

        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .add(android.R.id.content, new SettingsFragment())
                .commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onSaveInstanceState(Bundle instanceState) {
        instanceState.putInt("resourceState" , resourceState);
    }

    public void onResourceUpdated() {
        resourceState = 1;
        recreate();
    }



}

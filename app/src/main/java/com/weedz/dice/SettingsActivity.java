package com.weedz.dice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.OnResourceUpdatedListener{

    private static final String TAG = "SettingsActivity";
    private int resourceState = 0;

    protected void onCreate(Bundle savedInstanceState) {
        ViewUtils.ApplyTheme(this);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_about:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.action_help:
                intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                break;
            case R.id.set_default_settings:
                SharedPreferences tmpPref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = tmpPref.edit();
                editor.clear();
                editor.apply();
                PreferenceManager.setDefaultValues(this, R.xml.pref_main, true);
                recreate();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

}

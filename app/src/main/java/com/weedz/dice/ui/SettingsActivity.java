package com.weedz.dice.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.weedz.dice.R;
import com.weedz.dice.ViewUtils;
import com.weedz.dice.ui.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.OnResourceUpdatedListener {

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
                new AlertDialog.Builder(this)
                        .setTitle("Confirm")
                        .setMessage("Restore default settings?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences tmpPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                SharedPreferences.Editor editor = tmpPref.edit();
                                editor.clear();
                                editor.apply();
                                PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.pref_main, true);
                                recreate();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();

                break;

        }
        return super.onOptionsItemSelected(item);
    }

}

package com.weedz.dice.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.weedz.dice.R;
import com.weedz.dice.ViewUtils;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ViewUtils.ApplyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}

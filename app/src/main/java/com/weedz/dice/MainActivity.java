package com.weedz.dice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer {

    private static final String TAG = "HomeActivity";

    // IDs
    private int summaryTextFieldStart = 0x0000ffff;

    // Threading stuff
    private RollUpdateUIThread mRollUpdateThread;
    private SummaryUpdateUIThread mSummaryUpdateThread;
    private UpdateTableThread mUpdateTableThread;
    WeakReference<MainActivity> ref = new WeakReference<>(this);
    RollUpdateUIHandler handler = new RollUpdateUIHandler(ref);

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "onCreate()");
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        // Set default values for settings
        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false);
        ViewUtils.ApplyTheme(this, pref);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button bt_add_die = (Button)findViewById(R.id.add_die_button);
        bt_add_die.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRollUpdateThread != null) {
                    mRollUpdateThread.interrupt();
                    mRollUpdateThread = null;
                }
                if (mSummaryUpdateThread != null) {
                    mSummaryUpdateThread.interrupt();
                    mSummaryUpdateThread = null;
                }
                if (mUpdateTableThread != null) {
                    mUpdateTableThread.interrupt();
                    mUpdateTableThread = null;
                }
                EditText add_die_nr = (EditText)findViewById(R.id.add_die_nr);
                EditText add_die_sides = (EditText)findViewById(R.id.add_die_sides);
                try {
                    int nr = Integer.parseInt(add_die_nr.getText().toString());
                    int sides = Integer.parseInt(add_die_sides.getText().toString());
                    if (nr > 0 && sides > 1) {
                        Data.getInstance().addDice(nr, sides);
                    }
                } catch (NumberFormatException e) {
                    //Log.i(TAG, "AddDie():NumberFormatException");
                }
            }
        });
        Button bt_remove_die = (Button)findViewById(R.id.remove_die_button);
        bt_remove_die.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRollUpdateThread != null) {
                    mRollUpdateThread.interrupt();
                    mRollUpdateThread = null;
                }
                if (mSummaryUpdateThread != null) {
                    mSummaryUpdateThread.interrupt();
                    mSummaryUpdateThread = null;
                }
                if (mUpdateTableThread != null) {
                    mUpdateTableThread.interrupt();
                    mUpdateTableThread = null;
                }
                EditText remove_die_nr = (EditText)findViewById(R.id.remove_die_nr);
                EditText remove_die_sides = (EditText)findViewById(R.id.remove_die_sides);
                try {
                    int nr = Integer.parseInt(remove_die_nr.getText().toString());
                    int sides = Integer.parseInt(remove_die_sides.getText().toString());
                    if (nr > 0 && sides > 1) {
                        Data.getInstance().removeDie(nr, sides);
                    }
                } catch (NumberFormatException e) {
                    //Log.i(TAG, "RemoveDie():NumberFormatException");
                }
            }
        });
        Button bt_set_dice = (Button)findViewById(R.id.set_dice_button);
        bt_set_dice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRollUpdateThread != null) {
                    mRollUpdateThread.interrupt();
                    mRollUpdateThread = null;
                }
                if (mSummaryUpdateThread != null) {
                    mSummaryUpdateThread.interrupt();
                    mSummaryUpdateThread = null;
                }
                if (mUpdateTableThread != null) {
                    mUpdateTableThread.interrupt();
                    mUpdateTableThread = null;
                }
                EditText set_dice_nr = (EditText)findViewById(R.id.set_dice_nr);
                EditText set_dice_sides = (EditText)findViewById(R.id.set_dice_sides);
                try {
                    int nr = Integer.parseInt(set_dice_nr.getText().toString());
                    int sides = Integer.parseInt(set_dice_sides.getText().toString());
                    if (nr >= 0 && sides > 1) {
                        Data.getInstance().setDice(nr, sides);
                    }
                } catch (NumberFormatException e) {
                    //Log.i(TAG, "SetDie():NumberFormatException");
                }
            }
        });
        final Button roll = (Button)findViewById(R.id.roll_die_button);
        roll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRollUpdateThread != null) {
                    mRollUpdateThread.interrupt();
                    mRollUpdateThread = null;
                }
                if (mSummaryUpdateThread != null) {
                    mSummaryUpdateThread.interrupt();
                    mSummaryUpdateThread = null;
                }
                if (mUpdateTableThread != null) {
                    mUpdateTableThread.interrupt();
                    mUpdateTableThread = null;
                }
                TextView rolls = (TextView) ref.get().findViewById(R.id.die_rolls);
                TextView total_result = (TextView)findViewById(R.id.total_result);
                total_result.setText("Calculating...");
                if (pref.getBoolean("pref_settings_detailed_roll", true)) {
                    rolls.setText("Rolling...");
                } else {
                    rolls.setText("");
                }
                Data.getInstance().roll();
            }
        });

        showDices();
        Data.getInstance().addObserver(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, 1);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Settings activity
        if (requestCode == 1) {
            // Resources updated
            if (resultCode == 1) {
                recreate();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    static class RollUpdateUIHandler extends Handler {
        WeakReference<MainActivity> ref;

        public RollUpdateUIHandler(WeakReference<MainActivity> ref) {
            this.ref = ref;
        }

        @Override
        public void handleMessage(Message msg) {
            if (!Thread.currentThread().isInterrupted()) {
                //Log.d(TAG, "handler: " + msg.what);
                switch (msg.what) {
                    case 1: // All dice rolls finished
                        TextView rolls = (TextView) ref.get().findViewById(R.id.die_rolls);
                        rolls.setText((String)msg.obj);
                        break;
                    case 2: // Summary finished
                        if (ref.get().pref.getBoolean("pref_settings_summary", true)) {
                            int[] dice = (int[])msg.obj;
                            TableLayout tl = (TableLayout)ref.get().findViewById(R.id.dice_summary);
                            tl.removeAllViews();
                            // Three columns in table, calculate how rows we need
                            int rows = (int)Math.ceil(Data.getInstance().getLargestDie() / 3.0f);
                            TableRow[] tr = new TableRow[rows];
                            for (int i = 0;i < rows; i++) {
                                tr[i] = new TableRow(ref.get());
                            }

                            TextView[] textView = new TextView[Data.getInstance().getLargestDie()];

                            int trCounter;
                            for (int i = 0; i < textView.length; i++) {
                                trCounter = i / 3;

                                textView[i] = new TextView(ref.get());
                                textView[i].setTextAppearance(android.R.style.TextAppearance_Material_Medium);

                                if (dice[i] > 0) {
                                    textView[i].setTypeface(null, Typeface.BOLD);
                                } else {
                                    textView[i].setTypeface(null, Typeface.NORMAL);
                                }
                                textView[i].setText((i+1) + "s: " + dice[i]);
                                tr[trCounter].addView(textView[i]);
                            }
                            for (TableRow row: tr) {
                                tl.addView(row);
                            }
                        }
                        break;
                }
                // -----------------------------------------------------------------------------
                // Interrupted
                if (msg.what == 0) {
                    TableLayout tl = (TableLayout)ref.get().findViewById(R.id.dice_summary);
                    TableRow tr = new TableRow(ref.get());
                    TextView textView = new TextView(ref.get());
                    textView.setTextAppearance(android.R.style.TextAppearance_Material_Medium);
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    textView.setGravity(Gravity.CENTER_HORIZONTAL);
                    textView.setTypeface(null, Typeface.NORMAL);
                    textView.setText(R.string.interrupted);
                    tr.addView(textView);
                    tl.removeAllViews();
                    tl.addView(tr);
                }
                // Populate summary table
                if (msg.what == 4) {
                    TableLayout tl = (TableLayout)ref.get().findViewById(R.id.dice_summary);

                    SparseIntArray dice = (SparseIntArray)msg.obj;

                    for (int i = 0; i < dice.size(); i++) {
                        TextView existing = (TextView)tl.findViewWithTag(ref.get().summaryTextFieldStart + dice.keyAt(i) -  1);
                        if (existing != null) {
                            String str = existing.getText().toString();
                            int nr = Integer.parseInt(str.substring(str.indexOf(":") + 1)) + dice.get(dice.keyAt(i));

                            existing.setText(dice.keyAt(i) + ":" + Integer.toString(nr));
                        }
                    }
                }
                // Create summary table
                if (msg.what == 5) {
                    TableLayout tl = (TableLayout) ref.get().findViewById(R.id.dice_summary);
                    for (TableRow row :
                            (TableRow[])msg.obj) {
                        if (row != null) {
                            tl.addView(row);
                        }
                    }
                }
                if (msg.what == 6) {
                    TextView rolls = (TextView) ref.get().findViewById(R.id.die_rolls);
                    rolls.append((String)msg.obj);
                }
            }
            super.handleMessage(msg);
        }
    }

    private class SummaryUpdateUIThread extends Thread {

        public void run() {

            if (ref.get().pref.getBoolean("pref_settings_summary", true)) {
                int BUFFER_LENGTH = Integer.parseInt(ref.get().pref.getString("pref_Settings_summary_buffer", "100"));
                int threadSleep = Integer.parseInt(ref.get().pref.getString("pref_settings_summary_thread_sleep", "100"));

                SparseIntArray dice = new SparseIntArray();

                for (int i = 0; i < Data.getInstance().getNrOfDie(); i++) {
                    if (Thread.interrupted()) {
                        handler.sendEmptyMessage(0);
                        return;
                    }
                    if (Data.getInstance().getDie(i) == 0) {
                        //Log.d(TAG, "SummaryUpdateUIThread(): getDie(" + i + "):IndexOutOfBounds");
                        break;
                    }

                    if (dice.get(Data.getInstance().getDie(i)) == 0) {
                        dice.put(Data.getInstance().getDie(i), 1);
                    } else {
                        dice.put(Data.getInstance().getDie(i), dice.get(Data.getInstance().getDie(i)) + 1);
                    }
                    if (i > 0 && i % BUFFER_LENGTH == 0) {
                        handler.obtainMessage(4, dice.clone()).sendToTarget();
                        try {
                            Thread.sleep(threadSleep);
                        } catch (InterruptedException e) {
                            handler.sendEmptyMessage(0);
                            return;
                        }
                        dice.clear();
                    }
                }
                handler.obtainMessage(4, dice).sendToTarget();
            }
            synchronized (MainActivity.this) {
                mSummaryUpdateThread = null;
            }
        }
    }

    private class RollUpdateUIThread extends Thread {

        public void run() {
            if (ref.get().pref.getBoolean("pref_settings_detailed_roll", true)) {
                int bufferLength = Integer.parseInt(ref.get().pref.getString("pref_Settings_detailed_roll_buffer", "100"));
                int threadSleep = Integer.parseInt(ref.get().pref.getString("pref_settings_detailed_roll_thread_sleep", "100"));

                StringBuilder stringBuilder = new StringBuilder(Data.getInstance().getNrOfDie() * 2);
                stringBuilder.append("d").append(Data.getInstance().getLargestDie()).append("(");
                for (int i = 0; i < Data.getInstance().getNrOfDie(); i++) {
                    if (Thread.interrupted()) {
                        handler.sendEmptyMessage(0);
                        return;
                    }
                    stringBuilder.append(Data.getInstance().getDie(i)).append(",");
                }
                if (stringBuilder.length() > 0) {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }
                stringBuilder.append(")");

                // Buffer
                int start = 0;
                int end;
                //int bufferLength = Integer.parseInt(ref.get().pref.getString("pref_Settings_detailed_roll_buffer", "100"));
                while(start < stringBuilder.length()) {
                    if (Thread.interrupted()) {
                        handler.sendEmptyMessage(0);
                        return;
                    }
                    end = start + bufferLength;
                    if (end > stringBuilder.length()) {
                        end = stringBuilder.length();
                    }
                    handler.obtainMessage(6, stringBuilder.substring(start, end)).sendToTarget();
                    try {
                        Thread.sleep(threadSleep);
                    } catch (InterruptedException e) {
                        handler.sendEmptyMessage(0);
                        return;
                    }
                    start += bufferLength;
                }
            }

            synchronized (MainActivity.this) {
                mRollUpdateThread = null;
            }
        }
    }

    private class UpdateTableThread extends Thread {

        public void run() {
            if (ref.get().pref.getBoolean("pref_settings_summary", true)) {
                float textSize = Float.parseFloat(ref.get().pref.getString("pref_settings_summary_font_size", "20"));
                int ROW_BUFFER = Integer.parseInt(ref.get().pref.getString("pref_settings_create_summary_row_buffer", "50"));
                int COLUMNS = Integer.parseInt(ref.get().pref.getString("pref_settings_summary_table_columns", "50"));

                int counter = 0;
                int rows = (int) Math.ceil(Data.getInstance().getLargestDie() / (float)COLUMNS);
                TableRow[] tr = new TableRow[ROW_BUFFER];
                TextView[] tv;
                int rowIndex;

                // TODO: update for different sized dice

                for (int i = 0; i < rows; i++) {
                    if (Thread.interrupted()) {
                        handler.sendEmptyMessage(0);
                        return;
                    }
                    rowIndex = i % ROW_BUFFER;
                    tv = new TextView[COLUMNS];
                    tr[rowIndex] = new TableRow(ref.get());
                    for (int j = 0; j < COLUMNS; j++) {
                        tv[j] = new TextView(ref.get());
                        tv[j].setGravity(Gravity.CENTER_HORIZONTAL);
                        tv[j].setTag(ref.get().summaryTextFieldStart + counter);
                        tv[j].setTextAppearance(android.R.style.TextAppearance_Material_Medium);
                        tv[j].setTypeface(null, Typeface.NORMAL);
                        tv[j].setTextSize(textSize);
                        if (counter < Data.getInstance().getLargestDie()) {
                            int diceRoll = rowIndex * COLUMNS + j + 1;
                            tv[j].setText(diceRoll + ":0");
                        }
                        tr[rowIndex].addView(tv[j]);
                        counter++;
                    }
                    if (rowIndex == ROW_BUFFER - 1) {
                        handler.obtainMessage(5, tr.clone()).sendToTarget();
                        try {
                            Thread.sleep(Integer.parseInt(ref.get().pref.getString("pref_settings_create_summary_thread_sleep", "100")));
                        } catch (InterruptedException e) {
                            handler.sendEmptyMessage(0);
                            return;
                        }
                        tr = new TableRow[ROW_BUFFER];
                    }
                }
                handler.obtainMessage(5, tr.clone()).sendToTarget();
            }
            synchronized (MainActivity.this) {
                mUpdateTableThread = null;
            }
            populateTable();
        }
    }

    private synchronized void populateTable() {
        if (mSummaryUpdateThread != null) {
            mSummaryUpdateThread.interrupt();
            mSummaryUpdateThread = null;
        }
        mSummaryUpdateThread = new SummaryUpdateUIThread();
        mSummaryUpdateThread.start();
    }

    private synchronized void diceRolled() {
        int total = Data.getInstance().getTotal();
        TextView total_result = (TextView)findViewById(R.id.total_result);
        total_result.setText("" + total);

        TableLayout tl = (TableLayout)ref.get().findViewById(R.id.dice_summary);
        tl.removeAllViews();

        TextView rolls = (TextView) ref.get().findViewById(R.id.die_rolls);
        rolls.setTextSize(Float.parseFloat(ref.get().pref.getString("pref_settings_detailed_roll_thread_font_size", "19")));
        rolls.setText("");

        if (mRollUpdateThread != null) {
            mRollUpdateThread.interrupt();
            mRollUpdateThread = null;
        }
        if (mSummaryUpdateThread != null) {
            mSummaryUpdateThread.interrupt();
            mSummaryUpdateThread = null;
        }
        if (mUpdateTableThread != null) {
            mUpdateTableThread.interrupt();
            mUpdateTableThread = null;
        }

        mUpdateTableThread = new UpdateTableThread();
        mUpdateTableThread.start();

        mRollUpdateThread = new RollUpdateUIThread();
        mRollUpdateThread.start();

    }

    // TODO: Dices of different size
    private void showDices() {
        TextView dice = (TextView) findViewById(R.id.nrOfDice);
        dice.setText("Dice: " + Data.getInstance().getNrOfDie() + "d" + Data.getInstance().getLargestDie());
    }

    /*@Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        Data.getInstance().addObserver(this);
        super.onResume();
    }*/

    @Override
    protected void onStart() {
        //Log.d(TAG, "onStart()");
        Data.getInstance().addObserver(this);
        super.onStart();
    }

    /*@Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        if (mRollUpdateThread != null) {
            mRollUpdateThread.interrupt();
            mRollUpdateThread = null;
        }
        if (mSummaryUpdateThread != null) {
            mSummaryUpdateThread.interrupt();
            mSummaryUpdateThread = null;
        }
        Data.getInstance().deleteObserver(this);
        super.onPause();
    }*/

    @Override
    protected void onStop() {
        //Log.d(TAG, "onStop()");
        if (mRollUpdateThread != null) {
            mRollUpdateThread.interrupt();
            mRollUpdateThread = null;
        }
        if (mSummaryUpdateThread != null) {
            mSummaryUpdateThread.interrupt();
            mSummaryUpdateThread = null;
        }
        Data.getInstance().deleteObserver(this);
        super.onStop();
    }

    @Override
    public void update(Observable o, Object obj) {
        if (Data.getInstance().getFlag(Data.FLAG_DICE_ROLL)) {
            diceRolled();
            Data.getInstance().setFlag(Data.FLAG_DICE_ROLL, false);
        }
        if (Data.getInstance().getFlag(Data.FLAG_DICESET_UPDATE)) {
            showDices();
            Data.getInstance().setFlag(Data.FLAG_DICE_ROLL, false);
        }
        if (Data.getInstance().getFlag(Data.FLAG_INTERRUPTED)) {
            TextView total_result = (TextView)findViewById(R.id.total_result);
            total_result.setText(R.string.interrupted);
            Data.getInstance().setFlag(Data.FLAG_INTERRUPTED, false);
        }
    }
}
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
    WeakReference<MainActivity> ref = new WeakReference<>(this);
    RollUpdateUIHandler handler = new RollUpdateUIHandler(ref);

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        // Set default values for settings
        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false);

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
                TextView rolls = (TextView) ref.get().findViewById(R.id.die_rolls);
                TextView total_result = (TextView)findViewById(R.id.total_result);
                total_result.setText("Calculating...");
                if (pref.getBoolean("pref_settings_detailed_roll", true)) {
                    rolls.setText("Rolling...");
                } else {
                    rolls.setText("");
                }
                // Reset summary table
                /*TableLayout tl = (TableLayout)ref.get().findViewById(R.id.dice_summary);
                tl.removeAllViews();*/

                /*if (pref.getBoolean("pref_settings_summary", true)) {
                    TableRow tr = new TableRow(getApplicationContext());
                    TextView textView = new TextView(getApplicationContext());
                    textView.setTextAppearance(android.R.style.TextAppearance_Material_Medium_Inverse);
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    textView.setGravity(Gravity.CENTER_HORIZONTAL);
                    textView.setTypeface(null, Typeface.NORMAL);
                    textView.setText("Calculating...");
                    tr.addView(textView);
                    tl.addView(tr);
                }*/
                Data.getInstance().roll();
            }
        });

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
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
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
                if (msg.what == 0) {
                    TableLayout tl = (TableLayout)ref.get().findViewById(R.id.dice_summary);
                    tl.removeAllViews();
                    TableRow tr = new TableRow(ref.get());
                    TextView textView = new TextView(ref.get());
                    textView.setTextAppearance(android.R.style.TextAppearance_Material_Medium);
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    textView.setGravity(Gravity.CENTER_HORIZONTAL);
                    textView.setTypeface(null, Typeface.NORMAL);
                    textView.setText("Interrupted");
                    tr.addView(textView);
                    tl.addView(tr);
                }
                if (msg.what == 4) {
                    TableLayout tl = (TableLayout)ref.get().findViewById(R.id.dice_summary);

                    SparseIntArray dice = (SparseIntArray)msg.obj;

                    for (int i = 0; i < dice.size(); i++) {
                        TextView existing = (TextView)tl.findViewWithTag(ref.get().summaryTextFieldStart + dice.keyAt(i) -  1);
                        if (existing != null) {
                            String str = existing.getText().toString();
                            int nr = Integer.parseInt(str) + dice.get(dice.keyAt(i));

                            existing.setText(Integer.toString(nr));
                        }
                    }
                }
                // Create summary table
                if (msg.what == 5) {
                    int counter = msg.arg1;
                    int rows = msg.arg2;
                    TableLayout tl = (TableLayout) ref.get().findViewById(R.id.dice_summary);
                    TableRow tr;
                    TextView[] tv;    // 3 column // TODO: settings for columns?

                    for (int i = 0; i < rows; i++) {
                        tv = new TextView[3];
                        tr = new TableRow(ref.get());
                        for (TextView t :
                                tv) {
                            t = new TextView(ref.get());
                            t.setTag(ref.get().summaryTextFieldStart + counter);
                            t.setTextAppearance(android.R.style.TextAppearance_Material_Medium);
                            t.setTypeface(null, Typeface.NORMAL);
                            t.setText("0");
                            tr.addView(t);
                            counter++;
                        }
                        tl.addView(tr);
                    }
                }
                if (msg.what == 6) {
                    Log.d(TAG, "Die_roll: " + msg.obj);
                    TextView rolls = (TextView) ref.get().findViewById(R.id.die_rolls);
                    rolls.append((String)msg.obj);
                }
            }
            super.handleMessage(msg);
        }
    }

    private class SummaryUpdateUIThread extends Thread {
        public void run() {
            //Log.d(TAG, "New SummaryThread()");
            SparseIntArray dice = new SparseIntArray();

            //handler.obtainMessage(5, 0, (int)Math.ceil(Data.getInstance().getLargestDie() / 3.0f)).sendToTarget();

            if (ref.get().pref.getBoolean("pref_settings_summary", true)) {
                for (int i = 0; i < Data.getInstance().getNrOfDie(); i++) {
                    if (Thread.interrupted()) {
                        //Log.d(TAG, "SummaryUpdateUIThread(): interrupted");
                        handler.sendEmptyMessage(0);
                        return;
                    }
                    if (Data.getInstance().getDie(i) == 0) {
                        Log.d(TAG, "SummaryUpdateUIThread(): getDie(" + i + "):IndexOutOfBounds");
                        break;
                    }

                    if (dice.get(Data.getInstance().getDie(i)) == 0) {
                        dice.put(Data.getInstance().getDie(i), 1);
                    } else {
                        dice.put(Data.getInstance().getDie(i), dice.get(Data.getInstance().getDie(i)) + 1);
                    }
                    if (i > 0 && i % 500 == 0) {
                        handler.obtainMessage(4, dice.clone()).sendToTarget();
                        try {
                            Thread.sleep(Integer.parseInt(ref.get().pref.getString("pref_settings_summary_thread_sleep", "100")));
                        } catch (InterruptedException e) {
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
                StringBuilder stringBuilder = new StringBuilder(Data.getInstance().getNrOfDie() * 2);
                String die_rolls;
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
                //die_rolls = stringBuilder.toString();
                //handler.obtainMessage(1, die_rolls).sendToTarget();

                // Buffer
                int start = 0;
                int end;
                int bufferLength = 100;
                while(start < stringBuilder.length()) {
                    end = start + bufferLength;
                    if (end > stringBuilder.length()) {
                        end = stringBuilder.length();
                    }
                    handler.obtainMessage(6, stringBuilder.substring(start, end)).sendToTarget();
                    try {
                        Thread.sleep(Integer.parseInt(ref.get().pref.getString("pref_settings_detailed_roll_thread_sleep", "100")));
                    } catch (InterruptedException e) {
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

    private synchronized void diceRolled() {
        TableLayout tl = (TableLayout)ref.get().findViewById(R.id.dice_summary);
        tl.removeAllViews();
        TextView rolls = (TextView) ref.get().findViewById(R.id.die_rolls);
        rolls.setText("");

        if (ref.get().pref.getBoolean("pref_settings_summary", true)) {
            int counter = 0;
            int rows = (int) Math.ceil(Data.getInstance().getLargestDie() / 3.0f);
            TableRow tr;
            TextView[] tv;    // 3 column // TODO: settings for columns?

            for (int i = 0; i < rows; i++) {
                tv = new TextView[3];
                tr = new TableRow(ref.get());
                for (TextView t :
                        tv) {
                    t = new TextView(ref.get());
                    t.setTag(ref.get().summaryTextFieldStart + counter);
                    t.setTextAppearance(android.R.style.TextAppearance_Material_Medium);
                    t.setTypeface(null, Typeface.NORMAL);
                    t.setText("0");
                    tr.addView(t);
                    counter++;
                }
                tl.addView(tr);
            }
        }

        int total = Data.getInstance().getTotal();
        TextView total_result = (TextView)findViewById(R.id.total_result);
        total_result.setText("" + total);

        if (mRollUpdateThread != null) {
            mRollUpdateThread.interrupt();
            mRollUpdateThread = null;
        }
        if (mSummaryUpdateThread != null) {
            mSummaryUpdateThread.interrupt();
            mSummaryUpdateThread = null;
        }
        mSummaryUpdateThread = new SummaryUpdateUIThread();
        mSummaryUpdateThread.start();

        mRollUpdateThread = new RollUpdateUIThread();
        mRollUpdateThread.start();
    }

    // TODO: Dices of different size
    private void showDices() {
        TextView dice = (TextView) findViewById(R.id.nrOfDice);
        dice.setText("Dices: " + Data.getInstance().getNrOfDie() + "d" + Data.getInstance().getLargestDie());
    }

    @Override
    protected void onResume() {
        showDices();
        Data.getInstance().addObserver(this);
        super.onResume();
    }
    @Override
    protected void onPause() {
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
    }
    @Override
    protected void onStop() {
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
        if (Data.getInstance().getFlag(Data.FLAG_DIEBAG_UPDATE)) {
            showDices();
            Data.getInstance().setFlag(Data.FLAG_DICE_ROLL, false);
        }
    }
}

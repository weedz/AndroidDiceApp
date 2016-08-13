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

import com.weedz.dice.Bookmarks.BookmarksActivity;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer {

    private static final String TAG = "HomeActivity";

    // IDs
    private int summaryTextFieldStart = 0x000000ff;

    // Threading stuff
    private RollUpdateUIThread mRollUpdateThread;
    private PopulateSummaryTableThread mSummaryUpdateThread;
    private UpdateTableThread mUpdateTableThread;
    WeakReference<MainActivity> ref = new WeakReference<>(this);
    RollUpdateUIHandler handler = new RollUpdateUIHandler(ref);

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                        Data.getInstance().addMultiDice(nr, sides);
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
                        Data.getInstance().removeMultiDice(nr, sides);
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
                        Data.getInstance().setMultiDice(nr, sides);
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
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.action_bookmarks:
                intent = new Intent(this, BookmarksActivity.class);
                startActivity(intent);
                break;

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

                    int totalDice = Data.getInstance().getMultiNrOfDice();
                    for (int i = 1; i < dice.size(); i++) {
                        TextView existing = (TextView)tl.findViewWithTag(ref.get().summaryTextFieldStart + (dice.get(0) * totalDice) + dice.keyAt(i)-1);
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
                // Show detailed roll
                if (msg.what == 6) {
                    TextView rolls = (TextView) ref.get().findViewById(R.id.die_rolls);
                    rolls.append((String)msg.obj);
                }
            }
            super.handleMessage(msg);
        }
    }

    private class PopulateSummaryTableThread extends Thread {

        public void run() {
            final int BUFFER_LENGTH = Integer.parseInt(ref.get().pref.getString("pref_Settings_summary_buffer", "100"));
            final int THREAD_SLEEP = Integer.parseInt(ref.get().pref.getString("pref_settings_summary_thread_sleep", "100"));

            SparseIntArray dice = new SparseIntArray();
            // Multi dice
            for (Integer key :
                    Data.getInstance().getMultiDice().keySet()) {
                dice.put(0, key);
                for (int i = 0; i < Data.getInstance().getMultiNrOfDice(key); i++) {
                    if (Thread.interrupted()) {
                        handler.sendEmptyMessage(0);
                        return;
                    }
                    if (Data.getInstance().getMultiDie(key, i) == 0) {
                        //Log.d(TAG, "PopulateSummaryTableThread(): getMultiDie(" + key + ", " + i + "): IndexOutOfBounds");
                        break;
                    }
                    if (dice.get(Data.getInstance().getMultiDie(key, i)) == 0) {
                        dice.put(Data.getInstance().getMultiDie(key, i), 1);
                    } else {
                        dice.put(Data.getInstance().getMultiDie(key, i), dice.get(Data.getInstance().getMultiDie(key, i)) + 1);
                    }
                    if (i > 0 && i % BUFFER_LENGTH == 0) {
                        handler.obtainMessage(4, dice.clone()).sendToTarget();
                        try {
                            Thread.sleep(THREAD_SLEEP);
                        } catch (InterruptedException e) {
                            handler.sendEmptyMessage(0);
                            return;
                        }
                        dice.clear();
                        dice.put(0, key);
                    }
                }
                handler.obtainMessage(4, dice.clone()).sendToTarget();
                try {
                    Thread.sleep(THREAD_SLEEP);
                } catch (InterruptedException e) {
                    handler.sendEmptyMessage(0);
                    return;
                }
                dice.clear();
            }

            handler.obtainMessage(4, dice).sendToTarget();
            synchronized (MainActivity.this) {
                mSummaryUpdateThread = null;
            }
        }
    }

    private class RollUpdateUIThread extends Thread {

        public void run() {
            final int BUFFER_LENGTH = Integer.parseInt(ref.get().pref.getString("pref_Settings_detailed_roll_buffer", "100"));
            final int THREAD_SLEEP = Integer.parseInt(ref.get().pref.getString("pref_settings_detailed_roll_thread_sleep", "100"));

            StringBuilder stringBuilder = new StringBuilder(Data.getInstance().getMultiNrOfDice() * 2);
            // Multi dice
            for (Integer key :
                    Data.getInstance().getMultiDice().keySet()) {
                if (Thread.interrupted()) {
                    handler.sendEmptyMessage(0);
                    return;
                }
                stringBuilder.append(Data.getInstance().getMultiDice().get(key).size()).append("d").append(key).append("(");
                for (int i = 0; i < Data.getInstance().getMultiNrOfDice(key); i++) {
                    if (Thread.interrupted()) {
                        handler.sendEmptyMessage(0);
                        return;
                    }
                    stringBuilder.append(Data.getInstance().getMultiDie(key, i)).append(",");
                }
                if (stringBuilder.length() > 0) {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }
                stringBuilder.append(") ");
            }

            // Buffer
            int start = 0;
            int end;
            while(start < stringBuilder.length()) {
                if (Thread.interrupted()) {
                    handler.sendEmptyMessage(0);
                    return;
                }
                end = start + BUFFER_LENGTH;
                if (end > stringBuilder.length()) {
                    end = stringBuilder.length();
                }
                handler.obtainMessage(6, stringBuilder.substring(start, end)).sendToTarget();
                try {
                    Thread.sleep(THREAD_SLEEP);
                } catch (InterruptedException e) {
                    handler.sendEmptyMessage(0);
                    return;
                }
                start += BUFFER_LENGTH;
            }

            synchronized (MainActivity.this) {
                mRollUpdateThread = null;
            }
        }
    }

    private class UpdateTableThread extends Thread {

        public void run() {
            final float TEXT_SIZE = Float.parseFloat(ref.get().pref.getString("pref_settings_summary_font_size", "20"));
            final int ROW_BUFFER = Integer.parseInt(ref.get().pref.getString("pref_settings_create_summary_row_buffer", "50"));
            final int COLUMNS = Integer.parseInt(ref.get().pref.getString("pref_settings_summary_table_columns", "50"));

            // Multi dice
            int counter;
            TableRow[] tr = new TableRow[ROW_BUFFER];
            TextView[] tv;
            int rowIndex = 0;
            int rows;
            int rowbuffer_value = 0;
            int totalDice = Data.getInstance().getMultiNrOfDice();
            for (Integer key :
                    Data.getInstance().getMultiDice().keySet()) {
                rows = (int) Math.ceil(key / (float)COLUMNS) + 1;
                counter = 0;

                tv = new TextView[1];
                tv[0] = new TextView(ref.get());
                tv[0].setGravity(Gravity.CENTER_HORIZONTAL);
                tv[0].setTextAppearance(android.R.style.TextAppearance_Material_Medium);
                tv[0].setTypeface(null, Typeface.BOLD);
                tv[0].setTextSize(TEXT_SIZE);
                tv[0].setText(Data.getInstance().getMultiNrOfDice(key) + "d" + key);
                tr[rowIndex] = new TableRow(ref.get());
                tr[rowIndex].setGravity(Gravity.CENTER_HORIZONTAL);
                tr[rowIndex].addView(tv[0]);
                rowbuffer_value++;

                for (int i = 0; i < rows; i++) {
                    tv = new TextView[COLUMNS];
                    rowIndex = rowbuffer_value;
                    tr[rowIndex] = new TableRow(ref.get());
                    for (int j = 0; j < COLUMNS; j++) {
                        if (Thread.interrupted()) {
                            handler.sendEmptyMessage(0);
                            return;
                        }
                        tv[j] = new TextView(ref.get());
                        tv[j].setGravity(Gravity.CENTER_HORIZONTAL);
                        tv[j].setTextAppearance(android.R.style.TextAppearance_Material_Medium);
                        tv[j].setTypeface(null, Typeface.NORMAL);
                        tv[j].setTextSize(TEXT_SIZE);
                        if (counter < key) {
                            tv[j].setTag(ref.get().summaryTextFieldStart + (key * totalDice) + counter);
                            tv[j].setText((counter+1) + ":0");
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
                        rowbuffer_value = 0;
                    }
                    rowbuffer_value++;
                }
            }

            handler.obtainMessage(5, tr.clone()).sendToTarget();
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
        mSummaryUpdateThread = new PopulateSummaryTableThread();
        mSummaryUpdateThread.start();
    }

    // TODO: add buttons for disabled features
    private synchronized void diceRolled() {
        int total = Data.getInstance().getmTotal();
        TextView total_result = (TextView)findViewById(R.id.total_result);
        total_result.setText(String.valueOf(total));

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

        if (ref.get().pref.getBoolean("pref_settings_summary", true)) {
            mUpdateTableThread = new UpdateTableThread();
            mUpdateTableThread.start();
        }

        if (ref.get().pref.getBoolean("pref_settings_detailed_roll", true)) {
            mRollUpdateThread = new RollUpdateUIThread();
            mRollUpdateThread.start();
        }

    }

    private void showDices() {
        TextView dice = (TextView) findViewById(R.id.nrOfDice);

        StringBuilder diceSet = new StringBuilder("Dice: (").append(Data.getInstance().getMultiNrOfDice()).append("), ");
        for (Integer key :
                Data.getInstance().getMultiDice().keySet()) {
            diceSet.append(Data.getInstance().getMultiDice().get(key).size()).append("d").append(key).append(", ");
        }
        diceSet.delete(diceSet.length() - 2, diceSet.length() - 1);
        dice.setText(diceSet.toString());
    }

    @Override
    protected void onStart() {
        Data.getInstance().addObserver(this);
        showDices();
        super.onStart();
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

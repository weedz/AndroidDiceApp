package com.weedz.dice;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.weedz.dice.bookmarks.BookmarksActivity;
import com.weedz.dice.database.DiceDB;
import com.weedz.dice.database.HistoryDB;
import com.weedz.dice.history.HistoryActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer {

    private static final String TAG = "HomeActivity";

    // Threading stuff
    private RollUpdateUIThread mRollUpdateThread;
    private UpdateTableThread mUpdateTableThread;
    private WeakReference<MainActivity> ref = new WeakReference<>(this);
    private UpdateUIHandler handler = new UpdateUIHandler(ref);

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        // Set default values for settings
        DiceDB db = new DiceDB(this);
        db.close();
        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false);
        ViewUtils.ApplyTheme(this, pref);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bt_add_die = (Button)findViewById(R.id.add_die_button);
        bt_add_die.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRollUpdateThread != null) {
                    mRollUpdateThread.interrupt();
                    mRollUpdateThread = null;
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
                if (mUpdateTableThread != null) {
                    mUpdateTableThread.interrupt();
                    mUpdateTableThread = null;
                }
                EditText set_dice_nr = (EditText)findViewById(R.id.set_dice_nr);
                EditText set_dice_sides = (EditText)findViewById(R.id.set_dice_sides);
                try {
                    int nr = Integer.parseInt(set_dice_nr.getText().toString());
                    int sides = Integer.parseInt(set_dice_sides.getText().toString());
                    Data.getInstance().setMultiDice(nr, sides);
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
                if (mUpdateTableThread != null) {
                    mUpdateTableThread.interrupt();
                    mUpdateTableThread = null;
                }
                TextView total_result = (TextView)findViewById(R.id.total_result);
                total_result.setText("Calculating...");
                if (pref.getBoolean("pref_settings_detailed_roll", true)) {
                    TextView rolls = (TextView) ref.get().findViewById(R.id.die_rolls);
                    rolls.setText("");
                }
                LinearLayout table_container = (LinearLayout)findViewById(R.id.dice_summary_table_container);
                table_container.removeAllViews();

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
            case R.id.action_history:
                intent = new Intent(this, HistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.action_about:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.action_help:
                intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Settings activity
        if (requestCode == 1) {
            // Update resources
            if (resultCode == 1) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recreate();
                    }
                }, 0);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    static class UpdateUIHandler extends Handler {
        WeakReference<MainActivity> ref;

        public UpdateUIHandler(WeakReference<MainActivity> ref) {
            this.ref = ref;
        }

        @Override
        public void handleMessage(Message msg) {
            if (!Thread.currentThread().isInterrupted()) {
                // Interrupted
                if (msg.what == 0) {
                    if ((int)msg.obj == 2) {
                        LinearLayout table_container = (LinearLayout) ref.get().findViewById(R.id.dice_summary_table_container);
                        TableRow tr = new TableRow(ref.get());
                        TextView textView = new TextView(ref.get());
                        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        textView.setGravity(Gravity.CENTER_HORIZONTAL);
                        textView.setTypeface(null, Typeface.NORMAL);
                        textView.setText(R.string.interrupted);
                        tr.addView(textView);
                        table_container.removeAllViews();
                        table_container.addView(tr);
                    }
                    if ((int)msg.obj == 1) {
                        TextView rolls = (TextView) ref.get().findViewById(R.id.die_rolls);
                        rolls.setText(R.string.interrupted);
                    }
                    Thread.currentThread().interrupt();
                }
                // Fill summary scroll view with tables
                if (msg.what == 5) {
                    LinearLayout table_container = (LinearLayout) ref.get().findViewById(R.id.dice_summary_table_container);
                    for (View v :
                            (ArrayList<View>) msg.obj) {
                        table_container.addView(v);
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

    private class RollUpdateUIThread extends Thread {

        public void run() {

            StringBuilder sb = new StringBuilder(Data.getInstance().getMultiNrOfDice() * 2);
            // Multi dice
            for (Integer key :
                    Data.getInstance().getMultiDice().keySet()) {
                if (Thread.interrupted()) {
                    handler.obtainMessage(0,1).sendToTarget();
                    return;
                }
                sb.append(Data.getInstance().getMultiDice().get(key).size()).append("d").append(key).append("(");
                for (int i = 0; i < Data.getInstance().getMultiNrOfDice(key); i++) {
                    if (Thread.interrupted()) {
                        handler.obtainMessage(0,1).sendToTarget();
                        return;
                    }
                    sb.append(Data.getInstance().getMultiDie(key, i)).append(",");
                }
                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                sb.append(") ");
            }

            // Save to history
            if (pref.getBoolean("pref_settings_history", false)) {
                DiceDB mDBHelper = new DiceDB(getApplicationContext());
                SQLiteDatabase db = mDBHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(HistoryDB.History.COLUMN_NAME_DATA, sb.toString());
                db.insert(HistoryDB.History.TABLE_NAME, null, values);
                Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + HistoryDB.History.TABLE_NAME, null);
                c.moveToFirst();
                if (c.getInt(0) > Integer.parseInt(pref.getString("pref_settings_history_limit", "100"))) {
                    int limit = Integer.parseInt(pref.getString("pref_settings_history_limit", "100"));
                    db.execSQL("DELETE FROM " + HistoryDB.History.TABLE_NAME + " WHERE rowid not in (SELECT rowid FROM " +
                            HistoryDB.History.TABLE_NAME + " ORDER BY rowid desc LIMIT " + limit + ")");
                }
                c.close();
                db.close();
            }

            // Draw
            if (pref.getBoolean("pref_settings_detailed_roll", true)) {
                final int BUFFER_LENGTH = Integer.parseInt(pref.getString("pref_settings_detailed_roll_buffer", "100"));
                final int THREAD_SLEEP = Integer.parseInt(pref.getString("pref_settings_detailed_roll_thread_sleep", "100"));

                int start = 0;
                int end;
                while (start < sb.length()) {
                    if (Thread.interrupted()) {
                        handler.obtainMessage(0, 1).sendToTarget();
                        return;
                    }
                    end = start + BUFFER_LENGTH;
                    if (end > sb.length()) {
                        end = sb.length();
                    }
                    try {
                        Thread.sleep(THREAD_SLEEP);
                    } catch (InterruptedException e) {
                        handler.obtainMessage(0, 1).sendToTarget();
                        return;
                    }
                    handler.obtainMessage(6, sb.substring(start, end)).sendToTarget();
                    start += BUFFER_LENGTH;
                }
            }

            synchronized (MainActivity.this) {
                mRollUpdateThread = null;
            }
        }
    }

    private class UpdateTableThread extends Thread {

        public void run() {
            final float TEXT_SIZE = Float.parseFloat(pref.getString("pref_settings_summary_font_size", "20"));
            final int ROW_BUFFER = Integer.parseInt(pref.getString("pref_settings_create_summary_row_buffer", "500"));
            final int THREAD_SLEEP = Integer.parseInt(pref.getString("pref_settings_create_summary_thread_sleep", "0"));
            final int COLUMNS = Integer.parseInt(pref.getString("pref_settings_summary_table_columns", "3"));

            // Multi dice
            int counter;
            TableRow[] tr = new TableRow[ROW_BUFFER];
            TextView[] tv;
            TableLayout tl;
            int rowIndex = 0;
            int rows;
            int rowbuffer_value = 0;

            ArrayList<View> views = new ArrayList<>();

            for (Integer key :
                    Data.getInstance().getMultiDice().keySet()) {

                tl = new TableLayout(MainActivity.this);
                tl.setStretchAllColumns(true);
                tl.setTag(key);
                TextView tv_d = new TextView(getApplicationContext());
                tv_d.setGravity(Gravity.CENTER_HORIZONTAL);
                tv_d.setTextAppearance(ref.get(), android.R.style.TextAppearance_Medium);
                tv_d.setTypeface(null, Typeface.BOLD);
                tv_d.setTextSize(TEXT_SIZE);
                tv_d.setText(Data.getInstance().getMultiNrOfDice(key) + "d" + key);
                views.add(tv_d);
                views.add(tl);

                rows = (int) Math.ceil(key / (float)COLUMNS);
                counter = 0;

                for (int i = 0; i < rows; i++) {
                    tv = new TextView[COLUMNS];
                    rowIndex = rowbuffer_value;
                    rowbuffer_value++;
                    tr[rowIndex] = new TableRow(getApplicationContext());
                    for (int j = 0; j < COLUMNS; j++) {
                        if (Thread.interrupted()) {
                            handler.obtainMessage(0,2).sendToTarget();
                            return;
                        }
                        tv[j] = new TextView(getApplicationContext());
                        tv[j].setGravity(Gravity.CENTER_HORIZONTAL);
                        tv[j].setTextAppearance(ref.get(), android.R.style.TextAppearance_Medium);
                        tv[j].setTypeface(null, Typeface.NORMAL);
                        tv[j].setTextSize(TEXT_SIZE);
                        if (counter < key) {
                            int nr = Data.getInstance().getTotal(key, counter+1);
                            tv[j].setTag(counter);
                            tv[j].setText((counter + 1) + ":" + nr);
                        }
                        tr[rowIndex].addView(tv[j]);
                        counter++;
                    }
                    tl.addView(tr[rowIndex]);
                    if (rowbuffer_value >= ROW_BUFFER) {
                        try {
                            Thread.sleep(THREAD_SLEEP);
                        } catch (InterruptedException e) {
                            handler.obtainMessage(0,2).sendToTarget();
                            return;
                        }
                        handler.obtainMessage(5, views.clone()).sendToTarget();
                        views.clear();
                        rowbuffer_value = 0;
                        tr = new TableRow[ROW_BUFFER];
                    }
                }
                if (views.size() > 0) {
                    try {
                        Thread.sleep(THREAD_SLEEP);
                    } catch (InterruptedException e) {
                        handler.obtainMessage(0,2).sendToTarget();
                        return;
                    }
                    handler.obtainMessage(5, views.clone()).sendToTarget();
                    views.clear();
                }
            }
            synchronized (MainActivity.this) {
                mUpdateTableThread = null;
            }
        }
    }

    // TODO: add buttons for disabled features
    private synchronized void diceRolled() {
        int total = Data.getInstance().getTotal();
        TextView total_result = (TextView)findViewById(R.id.total_result);
        total_result.setText(String.valueOf(total));

        LinearLayout table_container = (LinearLayout)findViewById(R.id.dice_summary_table_container);
        table_container.removeAllViews();

        if (pref.getBoolean("pref_settings_detailed_roll", true)) {
            TextView rolls = (TextView) findViewById(R.id.die_rolls);
            rolls.setTextSize(Float.parseFloat(pref.getString("pref_settings_detailed_roll_thread_font_size", "19")));
            rolls.setText("");
        }

        if (mRollUpdateThread != null) {
            mRollUpdateThread.interrupt();
            mRollUpdateThread = null;
        }
        if (mUpdateTableThread != null) {
            mUpdateTableThread.interrupt();
            mUpdateTableThread = null;
        }

        if (Data.getInstance().getMultiDice().size() > 0) {
            if (pref.getBoolean("pref_settings_summary", true)) {
                mUpdateTableThread = new UpdateTableThread();
                mUpdateTableThread.start();
            }

            if (pref.getBoolean("pref_settings_detailed_roll", true) || pref.getBoolean("pref_settings_history", false)) {
                mRollUpdateThread = new RollUpdateUIThread();
                mRollUpdateThread.start();
            }
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

        RelativeLayout summary = (RelativeLayout) findViewById(R.id.view_summary);
        RelativeLayout detailed_roll = (RelativeLayout) findViewById(R.id.view_detailed_roll);
        TableLayout dice_set_controls = (TableLayout) findViewById(R.id.dice_set_control);
        if (!pref.getBoolean("pref_settings_detailed_roll", true)) {
            detailed_roll.setVisibility(View.GONE);
        } else {
            detailed_roll.setVisibility(View.VISIBLE);
        }
        if (!pref.getBoolean("pref_settings_summary", true)) {
            summary.setVisibility(View.GONE);
        } else {
            summary.setVisibility(View.VISIBLE);
        }
        if (!pref.getBoolean("pref_settings_dice_set_controls", true)) {
            dice_set_controls.setVisibility(View.GONE);
        } else {
            dice_set_controls.setVisibility(View.VISIBLE);
        }

        showDices();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mRollUpdateThread != null) {
            mRollUpdateThread.interrupt();
            mRollUpdateThread = null;
        }
        if (mUpdateTableThread != null) {
            mUpdateTableThread.interrupt();
            mUpdateTableThread = null;
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

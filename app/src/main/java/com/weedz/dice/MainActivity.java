package com.weedz.dice;

import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer {

    private static final String TAG = "HomeActivity";

    // Threading stuff
    private RollUpdateUIThread mRollUpdateThread;
    private SummaryUpdateUIThread mSummaryUpdateThread;
    WeakReference<MainActivity> ref = new WeakReference<>(this);
    RollUpdateUIHandler handler = new RollUpdateUIHandler(ref);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                    Log.i(TAG, "AddDie():NumberFormatException");
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
                    Log.i(TAG, "RemoveDie():NumberFormatException");
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
                    Log.i(TAG, "SetDie():NumberFormatException");
                }
            }
        });
        Button roll = (Button)findViewById(R.id.roll_die_button);
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
                rolls.setText("Rolling...");
                Data.getInstance().roll();
            }
        });

        Data.getInstance().addObserver(this);
    }

    static class RollUpdateUIHandler extends Handler {
        WeakReference<MainActivity> ref;

        public RollUpdateUIHandler(WeakReference<MainActivity> ref) {
            this.ref = ref;
        }

        @Override
        public void handleMessage(Message msg) {
            if (!Thread.currentThread().isInterrupted()) {
                switch (msg.what) {
                    case 0: // Interrupted
                        Toast.makeText(ref.get().getApplicationContext(), "Roll interrupted...", Toast.LENGTH_SHORT).show();
                        break;
                    case 1: // All dice rolls finished
                        TextView rolls = (TextView) ref.get().findViewById(R.id.die_rolls);
                        rolls.setText("d" + Data.getInstance().getLargestDie() + "(" + msg.obj + ")");
                        break;
                    case 2: // Summary finished
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

                        break;
                }
            }
            super.handleMessage(msg);
        }
    }

    private class SummaryUpdateUIThread extends Thread {
        public void run() {
            int[] dice = new int[Data.getInstance().getLargestDie()];
            for (int i = 0; i < Data.getInstance().getNrOfDie(); i++) {
                if(Thread.interrupted()) {
                    Log.d(TAG, "SummaryUpdateUIThread(): interrupted");
                    handler.sendEmptyMessage(0);
                    return;
                }
                dice[Data.getInstance().getDie(i)-1]++;
            }
            handler.obtainMessage(2, dice).sendToTarget();
            synchronized (MainActivity.this) {
                mSummaryUpdateThread = null;
            }
        }
    }

    private class RollUpdateUIThread extends Thread {
        public void run() {
            String die_rolls;
            StringBuilder stringBuilder = new StringBuilder(Data.getInstance().getNrOfDie()*2);
            for (int i = 0; i < Data.getInstance().getNrOfDie(); i++) {
                if(Thread.interrupted()) {
                    Log.d(TAG, "RollUpdateUIThread(): interrupted");
                    handler.sendEmptyMessage(0);
                    return;
                }
                stringBuilder.append(Data.getInstance().getDie(i)).append(",");
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            die_rolls = stringBuilder.toString();
            handler.obtainMessage(1, die_rolls).sendToTarget();
            synchronized (MainActivity.this) {
                mRollUpdateThread = null;
            }
        }
    }

    private synchronized void diceRolled() {
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

        int total = Data.getInstance().getTotal();
        TextView total_result = (TextView)findViewById(R.id.total_result);
        total_result.setText("" + total);
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
        Log.d(TAG, "update");
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

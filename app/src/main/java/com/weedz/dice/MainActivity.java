package com.weedz.dice;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer {

    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bt_add_die = (Button)findViewById(R.id.add_die_button);
        bt_add_die.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText add_die_nr = (EditText)findViewById(R.id.add_die_nr);
                int nr = Integer.parseInt(add_die_nr.getText().toString());
                Data.getInstance().addDie(nr);
            }
        });
        Button bt_remove_die = (Button)findViewById(R.id.remove_die_button);
        bt_remove_die.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText remove_die_nr = (EditText)findViewById(R.id.add_die_nr);
                int nr = Integer.parseInt(remove_die_nr.getText().toString());
                Data.getInstance().removeDie(nr);
            }
        });
        Button bt_set_dice = (Button)findViewById(R.id.set_dice_button);
        bt_set_dice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText set_dice_nr = (EditText)findViewById(R.id.set_dice_nr);
                int nr = Integer.parseInt(set_dice_nr.getText().toString());
                Data.getInstance().setDice(nr);
            }
        });
        Button roll = (Button)findViewById(R.id.roll_die_button);
        roll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Data.getInstance().roll();
                TextView rolls = (TextView)findViewById(R.id.die_rolls);
                TextView total_result = (TextView)findViewById(R.id.total_result);
                String die_rolls = "";
                int total = Data.getInstance().getTotal();
                for (int i = 0; i < Data.getInstance().getNrOfDie(); i++) {
                    die_rolls += Data.getInstance().getDie(i) + ",";
                }
                if (!die_rolls.equals("")) {
                    die_rolls = die_rolls.substring(0, die_rolls.length() - 1);
                }
                total_result.setText("" + total);
                rolls.setText("(" + die_rolls + ")");
                resultSummary();
            }
        });

        Data.getInstance().addObserver(this);
    }

    private void resultSummary() {
        int[] dice = new int[6];
        for (int i = 0; i < Data.getInstance().getNrOfDie(); i++) {
            switch (Data.getInstance().getDie(i)) {
                case 1:
                    dice[0]++;
                    break;
                case 2:
                    dice[1]++;
                    break;
                case 3:
                    dice[2]++;
                    break;
                case 4:
                    dice[3]++;
                    break;
                case 5:
                    dice[4]++;
                    break;
                case 6:
                    dice[5]++;
                    break;
            }
        }
        TextView summary = (TextView)findViewById(R.id.result_summary);
        summary.setText("1s: " + dice[0] + ", 2s: " + dice[1] + ", 3s: " +
                dice[2] + ", 4s: " + dice[3] + ", 5s: " + dice[4] + ", 6s: " + dice[5]);
        /*Toast.makeText(getApplicationContext(), "1s: " + dice[0] + ", 2s: " + dice[1] + ", 3s: " +
                dice[2] + ", 4s: " + dice[3] + ", 5s: " + dice[4] + ", 6s: " + dice[5], Toast.LENGTH_LONG).show();*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView dice = (TextView) findViewById(R.id.nrOfDice);
        dice.setText("Dices: " + Data.getInstance().getNrOfDie());
    }

    @Override
    public void update(Observable o, Object obj) {
        TextView dice = (TextView) findViewById(R.id.nrOfDice);
        dice.setText("Dices: " + Data.getInstance().getNrOfDie());


    }
}

package com.weedz.dice.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.weedz.dice.R;
import com.weedz.dice.ViewUtils;
import com.weedz.dice.database.DiceDB;
import com.weedz.dice.database.HistoryDB;
import com.weedz.dice.history.HistoryListAdapter;

import java.util.ArrayList;

/*

TODO: change ListView to ExpandableListView
 */

public class HistoryActivity extends AppCompatActivity {
    private static final String TAG = "HistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ViewUtils.ApplyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Fill history list view
        ArrayList<String> values = new ArrayList<>();
        ArrayList<Long> timestamp = new ArrayList<>();

        DiceDB mDBHelper = new DiceDB(getApplicationContext());
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String[] returnValues = {
                HistoryDB.History.COLUMN_NAME_ENTRY_ID,
                HistoryDB.History.COLUMN_NAME_DATA,
                HistoryDB.History.COLUMN_NAME_TIMESTAMP
        };
        String sortOrder = HistoryDB.History.COLUMN_NAME_ENTRY_ID + " DESC";
        Cursor c = db.query(
                HistoryDB.History.TABLE_NAME,
                returnValues,
                null,
                null,
                null,
                null,
                sortOrder
        );

        while (c.moveToNext()) {
            values.add(c.getString(1));
            timestamp.add(Long.parseLong(c.getString(2)));
        }

        final ListView history = (ListView)findViewById(R.id.history_listview);
        final HistoryListAdapter adapter = new HistoryListAdapter(this, R.layout.history_row_layout, values, timestamp);
        history.setAdapter(adapter);
        c.close();
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                Context context = this;
                new AlertDialog.Builder(context)
                        .setTitle("Confirm")
                        .setMessage("Clear history")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            // Clear history
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DiceDB mDBHelper = new DiceDB(getApplicationContext());
                                SQLiteDatabase db = mDBHelper.getReadableDatabase();
                                db.execSQL("DELETE FROM " + HistoryDB.History.TABLE_NAME);
                                db.close();
                                final ListView history = (ListView)findViewById(R.id.history_listview);
                                ArrayAdapter adapter = (ArrayAdapter)history.getAdapter();
                                adapter.clear();
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

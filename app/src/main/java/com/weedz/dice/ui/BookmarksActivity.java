package com.weedz.dice.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.weedz.dice.Data;
import com.weedz.dice.R;
import com.weedz.dice.ViewUtils;
import com.weedz.dice.bookmarks.BookmarksListAdapter;
import com.weedz.dice.database.BookmarksDB;
import com.weedz.dice.database.DiceDB;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class BookmarksActivity extends AppCompatActivity {
    private static final String TAG = "BookmarksActivity";

    private boolean actionChecked = false;
    private boolean checkItems = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ViewUtils.ApplyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ListView bookmarksList = (ListView)findViewById(R.id.bookmarks_saved_list);

        // Load dice-set from DB
        DiceDB mDBHelper = new DiceDB(getApplicationContext());
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String[] returnValues = {
                BookmarksDB.Bookmarks.COLUMN_NAME_ENTRY_ID,
                BookmarksDB.Bookmarks.COLUMN_NAME_SAVE_NAME,
                BookmarksDB.Bookmarks.COLUMN_NAME_SAVE_NR,
                BookmarksDB.Bookmarks.COLUMN_NAME_SAVE_SIDES
        };
        String sortOrder = BookmarksDB.Bookmarks.COLUMN_NAME_SAVE_NAME + " ASC";
        Cursor c = db.query(
                BookmarksDB.Bookmarks.TABLE_NAME,
                returnValues,
                null,
                null,
                null,
                null,
                sortOrder
        );

        final ArrayList<String> nameList = new ArrayList<>();
        final ArrayList<String> idList = new ArrayList<>();
        final ArrayList<ArrayList<Integer>> nrList = new ArrayList<>();
        final ArrayList<ArrayList<Integer>> sidesList = new ArrayList<>();
        if (c.getCount() > 0) {
            // ObjectStream
            ByteArrayInputStream bis;
            ObjectInput in;
            while (c.moveToNext()) {
                idList.add(c.getString(0));
                nameList.add(c.getString(1));
                try {
                    bis = new ByteArrayInputStream(c.getBlob(2));
                    in = new ObjectInputStream(bis);
                    nrList.add((ArrayList<Integer>)in.readObject());
                    bis.reset();
                    bis = new ByteArrayInputStream(c.getBlob(3));
                    in = new ObjectInputStream(bis);
                    sidesList.add((ArrayList<Integer>)in.readObject());
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        c.close();
        db.close();
        final BookmarksListAdapter adapter = new BookmarksListAdapter(this, R.layout.bookmarks_row_layout, idList, nameList, sidesList, nrList);
        bookmarksList.setAdapter(adapter);

        Button save = (Button)findViewById(R.id.bookmarks_save_button);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Data.getInstance().getMultiDice().size() == 0) {
                    return;
                }
                // Save dice set to SQLite DB
                DiceDB mDBHelper = new DiceDB(getApplicationContext());
                SQLiteDatabase db = mDBHelper.getWritableDatabase();

                // Build arrays
                ArrayList<Integer> nrArray = new ArrayList<>();
                ArrayList<Integer> sidesArray = new ArrayList<>();
                for (Integer key :
                        Data.getInstance().getMultiDice().keySet()) {
                    nrArray.add(Data.getInstance().getMultiNrOfDice(key));
                    sidesArray.add(key);
                }
                // ObjectStream
                ByteArrayOutputStream bos;
                ObjectOutput out;
                byte[] nrBytes;
                byte[] sidesBytes;
                try {
                    bos = new ByteArrayOutputStream();
                    out = new ObjectOutputStream(bos);
                    out.writeObject(nrArray);
                    nrBytes = bos.toByteArray();
                    bos.flush();
                    bos.reset();
                    out = new ObjectOutputStream(bos);
                    out.writeObject(sidesArray);
                    sidesBytes = bos.toByteArray();
                    bos.flush();

                } catch (IOException e) {
                    return;
                }

                EditText nameBox = (EditText)findViewById(R.id.bookmarks_save_name_editbox);
                String name = nameBox.getText().toString();
                if (name.equals("")) {
                    name = "DiceSet";
                }
                ContentValues values = new ContentValues();
                values.put(BookmarksDB.Bookmarks.COLUMN_NAME_SAVE_NAME, name);
                values.put(BookmarksDB.Bookmarks.COLUMN_NAME_SAVE_NR, nrBytes);
                values.put(BookmarksDB.Bookmarks.COLUMN_NAME_SAVE_SIDES, sidesBytes);

                Long newRowId = db.insert(
                        BookmarksDB.Bookmarks.TABLE_NAME,
                        null,
                        values
                );
                db.close();

                // Update ListView
                nrList.add(nrArray);
                sidesList.add(sidesArray);
                nameList.add(name);
                idList.add(newRowId.toString());

                adapter.notifyDataSetChanged();
            }
        });
        final ToggleButton actionButton = (ToggleButton)findViewById(R.id.bookmarks_actions_button);
        actionChecked = actionButton.isChecked();
        actionButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                actionChecked = isChecked;
            }
        });

        final Context context = this;
        Button clear = (Button)findViewById(R.id.bookmarks_clear_button);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("Confirm")
                        .setMessage("Clear saved bookmarks")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DiceDB mDBHelper = new DiceDB(getApplicationContext());
                                SQLiteDatabase db = mDBHelper.getReadableDatabase();
                                db.execSQL("DELETE FROM " + BookmarksDB.Bookmarks.TABLE_NAME);
                                db.close();
                                idList.clear();
                                nameList.clear();
                                nrList.clear();
                                sidesList.clear();
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });


        bookmarksList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> adapterView, View item, int position, long l) {
                checkItems = true;
                bookmarksList.setItemChecked(position, true);

                Toast.makeText(getApplicationContext(), "Long click", Toast.LENGTH_SHORT).show();
                return true;

            }

        });

        bookmarksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View item, int position, long l) {

                if (checkItems) {
                    bookmarksList.setItemChecked(position, true);
                    return;
                }

                String id = adapter.getItem(position);
                DiceDB mDBHelper = new DiceDB(getApplicationContext());
                SQLiteDatabase db = mDBHelper.getReadableDatabase();
                if (actionChecked) {
                    // Load
                    String[] returnValues = {
                            BookmarksDB.Bookmarks.COLUMN_NAME_ENTRY_ID,
                            BookmarksDB.Bookmarks.COLUMN_NAME_SAVE_NAME,
                            BookmarksDB.Bookmarks.COLUMN_NAME_SAVE_NR,
                            BookmarksDB.Bookmarks.COLUMN_NAME_SAVE_SIDES
                    };
                    Cursor c = db.query(
                            BookmarksDB.Bookmarks.TABLE_NAME,
                            returnValues,
                            BookmarksDB.Bookmarks.COLUMN_NAME_ENTRY_ID + " = ?",
                            new String[]{id},
                            null,
                            null,
                            null
                    );

                    if (c.getCount() != 0) {
                        ArrayList<Integer> nrList = new ArrayList<>();
                        ArrayList<Integer> sidesList = new ArrayList<>();

                        // ObjectStream
                        ByteArrayInputStream bis;
                        ObjectInput in;
                        c.moveToNext();

                        try {
                            bis = new ByteArrayInputStream(c.getBlob(2));
                            in = new ObjectInputStream(bis);
                            nrList = (ArrayList<Integer>)in.readObject();
                            bis.reset();
                            bis = new ByteArrayInputStream(c.getBlob(3));
                            in = new ObjectInputStream(bis);
                            sidesList = (ArrayList<Integer>)in.readObject();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        // set multidice
                        Data.getInstance().setMultiDice(0,0);
                        for (int i = 0; i < nrList.size(); i++) {
                            Data.getInstance().addMultiDice(nrList.get(i),
                                    sidesList.get(i));
                        }
                        Toast.makeText(BookmarksActivity.this, c.getString(1) + " loaded", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    c.close();
                } else {
                    // Remove
                    db.delete(
                            BookmarksDB.Bookmarks.TABLE_NAME,
                            BookmarksDB.Bookmarks.COLUMN_NAME_ENTRY_ID + " = ?",
                            new String[]{id}
                    );
                    db.close();
                    idList.remove(position);
                    nameList.remove(position);
                    nrList.remove(position);
                    sidesList.remove(position);
                    adapter.notifyDataSetChanged();
                }
                db.close();
            }
        });
    }
}

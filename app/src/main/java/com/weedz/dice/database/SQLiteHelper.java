package com.weedz.dice.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by WeeDz on 2016-08-12.
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String TAG = "SQLiteHelper";

    private static final int DATABASE_VERSION = 8;
    private static final String DATABASE_NAME = "bookmarks.db";

    /* Inner class that defines the table contents */
    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "bookmarks";
        public static final String COLUMN_NAME_ENTRY_ID = "entry_id";
        public static final String COLUMN_NAME_SAVE_NAME = "save_name";
        public static final String COLUMN_NAME_SAVE_NR = "save_nr";
        public static final String COLUMN_NAME_SAVE_SIDES = "save_sides";
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry.COLUMN_NAME_ENTRY_ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_SAVE_NAME + " TEXT," +
                    FeedEntry.COLUMN_NAME_SAVE_NR + " TEXT," +
                    FeedEntry.COLUMN_NAME_SAVE_SIDES + " TEXT" +
            " )";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Create table: " + FeedEntry.TABLE_NAME);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // new database structure
        db.execSQL("DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME);
        onCreate(db);
    }
}

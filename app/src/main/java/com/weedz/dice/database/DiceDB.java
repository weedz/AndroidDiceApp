package com.weedz.dice.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by WeeDz on 2016-08-14.
 */
public class DiceDB extends SQLiteHelper {
    protected static final int DATABASE_VERSION = 4;

    public DiceDB(Context context) {
        super(context, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(HistoryDB.SQL_CREATE_ENTRIES);
        db.execSQL(BookmarksDB.SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // new database structure
        db.execSQL("DROP TABLE IF EXISTS " + BookmarksDB.Bookmarks.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HistoryDB.History.TABLE_NAME);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BookmarksDB.Bookmarks.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HistoryDB.History.TABLE_NAME);
        onCreate(db);
    }
}

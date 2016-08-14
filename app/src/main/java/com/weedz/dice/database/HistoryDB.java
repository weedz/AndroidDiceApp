package com.weedz.dice.database;

import android.provider.BaseColumns;

/**
 * Created by WeeDz on 2016-08-14.
 */
public abstract class HistoryDB {
    protected static final int DATABASE_VERSION = 1;

    public static abstract class History implements BaseColumns {
        public static final String TABLE_NAME = "history";
        public static final String COLUMN_NAME_ENTRY_ID = "entry_id";
        public static final String COLUMN_NAME_DATA = "data";
    }

    protected static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " +
                    History.TABLE_NAME + "(" +
                    History.COLUMN_NAME_ENTRY_ID + " INTEGER PRIMARY KEY," +
                    History.COLUMN_NAME_DATA + " TEXT" +
                    " )";
}

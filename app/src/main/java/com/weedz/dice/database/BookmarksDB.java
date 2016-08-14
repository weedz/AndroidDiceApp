package com.weedz.dice.database;

import android.provider.BaseColumns;

/**
 * Created by WeeDz on 2016-08-14.
 */
public abstract class BookmarksDB {

    protected static final int DATABASE_VERSION = 1;

    public static abstract class Bookmarks implements BaseColumns {
        public static final String TABLE_NAME = "bookmarks";
        public static final String COLUMN_NAME_ENTRY_ID = "entry_id";
        public static final String COLUMN_NAME_SAVE_NAME = "save_name";
        public static final String COLUMN_NAME_SAVE_NR = "save_nr";
        public static final String COLUMN_NAME_SAVE_SIDES = "save_sides";
    }

    protected static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Bookmarks.TABLE_NAME + " (" +
                    Bookmarks.COLUMN_NAME_ENTRY_ID + " INTEGER PRIMARY KEY," +
                    Bookmarks.COLUMN_NAME_SAVE_NAME + " TEXT," +
                    Bookmarks.COLUMN_NAME_SAVE_NR + " TEXT," +
                    Bookmarks.COLUMN_NAME_SAVE_SIDES + " TEXT" +
                    " )";

}

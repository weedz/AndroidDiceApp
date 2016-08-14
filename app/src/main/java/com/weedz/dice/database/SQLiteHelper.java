package com.weedz.dice.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by WeeDz on 2016-08-12.
 */
abstract class SQLiteHelper extends SQLiteOpenHelper {
    private static final String TAG = "SQLiteHelper";
    protected static final String DATABASE_NAME = "dice.db";

    public SQLiteHelper(Context context, int version) {
        super(context, DATABASE_NAME, null, version);
    }
}

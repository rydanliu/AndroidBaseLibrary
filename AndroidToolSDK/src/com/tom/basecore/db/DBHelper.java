package com.tom.basecore.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Description:
 * User£º yuanzeyao.
 * Date£º 2015-09-02 18:39
 */
public class DBHelper extends SQLiteOpenHelper {

    private Class<?>[] tableClass;
    private static final int DB_VERSION=1;

    private static final String DB_NAME="android.db";

    public DBHelper(Context mContext,Class<?>[] tableClass){
        super(mContext,DB_NAME,null,DB_VERSION);
        this.tableClass=tableClass;

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        TableHelper.createTableByClasses(db,tableClass);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

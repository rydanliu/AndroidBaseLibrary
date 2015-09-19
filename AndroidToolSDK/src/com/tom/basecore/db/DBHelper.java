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
    public static final String TAG="DBHelper";
    public DBHelper(Context mContext,Class<?>[] tableClass){
        super(mContext,DBConfig.DB_ONE_NAME,null,DBConfig.DB_ONE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        TableHelper.createTableByClasses(db,DBConfig.TABLES_ONE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

package com.tom.basecore.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Description:
 * User�� yuanzeyao.
 * Date�� 2015-09-17 16:39
 */
public class BasicDBDao {
    /**
     * �õ���ֵ.
     * @param columnName the column name
     * @param cursor the cursor
     * @return the string column value
     */
    public String getStringColumnValue(String columnName, Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    /**
     * �õ���ֵ.
     * @param columnName the column name
     * @param cursor the cursor
     * @return the int column value
     */
    public int getIntColumnValue(String columnName, Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    /**
     * �������ر����ݿ����α�.
     * @param cursor the cursor
     * @param db the db
     */
    public void closeDatabase(Cursor cursor, SQLiteDatabase db) {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        if (db != null && db.isOpen()) {
            db.close();
            db = null;
        }
    }

    /**
     * �������ر��α�.
     * @param cursor the cursor
     */
    public void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
    }
}

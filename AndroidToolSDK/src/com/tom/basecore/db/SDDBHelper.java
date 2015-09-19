package com.tom.basecore.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

/**
 * Description:
 * User�� yuanzeyao.
 * Date�� 2015-09-16 18:28
 */
public class SDDBHelper extends SQLiteOpenHelper{
    private final Context mContext;
    private final String mName;
    private final String mDir;
    private final SQLiteDatabase.CursorFactory mFactory;
    private final int mNewVersion;
    private SQLiteDatabase mDatabase = null;
    private boolean mIsInitializing = false;

    public SDDBHelper(Context mContext){
        super(mContext,DBConfig.DB_TWO_NAME,null,DBConfig.DB_TWO_VERSION);
        if(DBConfig.DB_TWO_VERSION<1){
            throw new IllegalArgumentException("Version must be >= 1, was " + DBConfig.DB_TWO_VERSION);
        }
        this.mContext=mContext;
        this.mName=DBConfig.DB_TWO_NAME;
        this.mDir=DBConfig.DB_DIR;
        this.mFactory=null;
        this.mNewVersion=DBConfig.DB_TWO_VERSION;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        TableHelper.createTableByClasses(db,DBConfig.TABLES_TWO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public synchronized SQLiteDatabase getWritableDatabase() {
        if (mDatabase != null && mDatabase.isOpen() && !mDatabase.isReadOnly()) {
            //�Ѿ���ȡ��
            return mDatabase;
        }
        if (mIsInitializing) {
            throw new IllegalStateException("���ݿ��ѱ�ռ��getWritableDatabase()");
        }
        boolean success = false;
        SQLiteDatabase db = null;
        try {
            mIsInitializing = true;
            if (mName == null) {
                //����һ���ڴ�֧��SQLite���ݿ�
                db = SQLiteDatabase.create(null);
            } else {
                //����һ���ļ�֧��SQLite���ݿ�
                String path = mDir + File.separator + mName;
                db = SQLiteDatabase.openOrCreateDatabase(path,mFactory);
            }
            int version = db.getVersion();
            if (version != mNewVersion) {
                db.beginTransaction();
                try {
                    if (version == 0) {
                        onCreate(db);
                    } else {
                        onUpgrade(db, version, mNewVersion);
                    }
                    db.setVersion(mNewVersion);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
            onOpen(db);
            success = true;
            return db;
        } finally {
            //�ͷ�ռ��
            mIsInitializing = false;
            if (success) {
                if (mDatabase != null) {
                    try {
                        mDatabase.close();
                    } catch (Exception e) {
                    }
                }
                mDatabase = db;
            } else {
                if (db != null) db.close();
            }
        }
    }

    public synchronized SQLiteDatabase getReadableDatabase() {
        if (mDatabase != null && mDatabase.isOpen()) {
            //�Ѿ���ȡ��
            return mDatabase;
        }
        if (mIsInitializing) {
            throw new IllegalStateException("���ݿ��ѱ�ռ��getReadableDatabase()");
        }

        //����д��ȡд�����ݿ�
        SQLiteDatabase db = null;

        try {
            db = getWritableDatabase();
            mDatabase = db;
        } catch (Exception e1) {
            try {
                mIsInitializing = true;
                String path = mDir + File.separator + mName;
                db = SQLiteDatabase.openDatabase(path, mFactory, SQLiteDatabase.OPEN_READONLY);
                if (db.getVersion() != mNewVersion) {
                    throw new SQLiteException("���ܸ���ֻ�����ݿ�İ汾 from version " +
                            db.getVersion() + " to " + mNewVersion + ": " + path);
                }
                onOpen(db);
                mDatabase = db;
                return mDatabase;
            }catch (SQLiteException e) {

            } finally {
                mIsInitializing = false;
                if (db != null && db != mDatabase) db.close();
            }
        }

        return mDatabase;
    }
}

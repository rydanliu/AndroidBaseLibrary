package com.tom.basecore.db.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.tom.basecore.db.TableHelper;
import com.tom.basecore.db.annotation.Column;
import com.tom.basecore.db.annotation.Id;
import com.tom.basecore.db.annotation.Table;
import com.tom.basecore.utlis.DebugLog;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Description:数据库操作API
 * User： yuanzeyao.
 * Date： 2015-09-17 16:42
 */
public class DBDaoImpl<T> extends BasicDBDao implements IDBDao<T> {
    public static final String TAG="DBDaoImpl";

    /** The db helper. */
    private SQLiteOpenHelper dbHelper;

    /** 锁对象. */
    private final ReentrantLock lock = new ReentrantLock();

    /** The table name. */
    private String tableName;

    /** The id column. */
    private String idColumn;

    /** The clazz. */
    private Class<T> clazz;

    /** The all fields. */
    private List<Field> allFields;

    /** The Constant METHOD_INSERT. */
    private static final int METHOD_INSERT = 0;

    /** The Constant METHOD_UPDATE. */
    private static final int METHOD_UPDATE = 1;

    /** The Constant TYPE_NOT_INCREMENT. */
    private static final int TYPE_NOT_INCREMENT = 0;

    /** The Constant TYPE_INCREMENT. */
    private static final int TYPE_INCREMENT = 1;

    /** 这个Dao的数据库对象. */
    private SQLiteDatabase db = null;

    /**
     * 用一个对象实体初始化这个数据库操作实现类.
     *
     * @param dbHelper 数据库操作实现类
     * @param clazz 映射对象实体
     */
    public DBDaoImpl(SQLiteOpenHelper dbHelper, Class<T> clazz) {
        this.dbHelper = dbHelper;
        if (clazz == null) {
            this.clazz = ((Class<T>) ((java.lang.reflect.ParameterizedType) super
                    .getClass().getGenericSuperclass())
                    .getActualTypeArguments()[0]);
        } else {
            this.clazz = clazz;
        }

        if (this.clazz.isAnnotationPresent(Table.class)) {
            Table table = (Table) this.clazz.getAnnotation(Table.class);
            this.tableName = table.name();
        }

        // 加载所有字段
        this.allFields = TableHelper.joinFieldsOnlyColumn(this.clazz.getDeclaredFields(),
                this.clazz.getSuperclass().getDeclaredFields());

        // 找到主键
        for (Field field : this.allFields) {
            if (field.isAnnotationPresent(Id.class)) {
                Column column = (Column) field.getAnnotation(Column.class);
                this.idColumn = column.name();
                break;
            }
        }

        DebugLog.d(TAG, "clazz:" + this.clazz + " tableName:" + this.tableName
                + " idColumn:" + this.idColumn);
    }

    /**
     * 初始化这个数据库操作实现类.
     *
     * @param dbHelper 数据库操作实现类
     */
    public DBDaoImpl(SQLiteOpenHelper dbHelper) {
        this(dbHelper, null);
    }

    /**
     * 拿到{@link # SQLiteOpenHelper}对象
     *
     * @return the db helper
     * @see
     */
    @Override
    public SQLiteOpenHelper getDbHelper() {
        return dbHelper;
    }

    @Override
    public long insert(T entity) {
        return insert(entity, true);
    }

    /**
     * 描述：插入实体.
     * @param entity the entity
     * @param flag the flag
     * @return the long
     * @see IDBDao#insert(Object, boolean)
     */
    @Override
    public long insert(T entity, boolean flag) {
        String sql = null;
        long rowId = -1;
        try {
            lock.lock();
            checkDBOpened();
            ContentValues cv = new ContentValues();
            if (flag) {
                // id自增
                sql = setContentValues(entity, cv, TYPE_INCREMENT,METHOD_INSERT);
            } else {
                // id需指定
                sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT,METHOD_INSERT);
            }
            DebugLog.d(TAG, "[insert]: insert into " + this.tableName + " " + sql);
            rowId = db.insert(this.tableName, null, cv);



        } catch (Exception e) {
            DebugLog.d(TAG, "[insert] into DB Exception.");
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return rowId;
    }

    @Override
    public long[] insertList(List<T> entityList) {
        return insertList(entityList, true);
    }

    @Override
    public long[] insertList(List<T> entityList, boolean flag) {
        String sql = null;
        long[] rowIds = new long[entityList.size()];
        for(int i=0;i<rowIds.length;i++){
            rowIds[i] = -1;
        }
        try {
            lock.lock();
            checkDBOpened();
            for(int i=0;i<entityList.size();i++){
                T entity  = entityList.get(i);
                ContentValues cv = new ContentValues();
                if (flag) {
                    // id自增
                    sql = setContentValues(entity, cv, TYPE_INCREMENT,METHOD_INSERT);
                } else {
                    // id需指定
                    sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT,METHOD_INSERT);
                }

                DebugLog.d(TAG, "[insertList]: insert into " + this.tableName + " " + sql);
                rowIds[i] = db.insert(this.tableName, null, cv);


            }
        } catch (Exception e) {
            DebugLog.d(TAG, "[insertList] into DB Exception.");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return rowIds;
    }

    /**
     * 描述：按id删除.
     *
     * @param id the id
     * @return the int
     * @see IDBDao#delete(int)
     */
    @Override
    public int delete(int id) {
        int rows = 0;
        try {
            lock.lock();
            checkDBOpened();
            String where = this.idColumn + " = ?";
            String[] whereValue = { Integer.toString(id) };
            DebugLog.d(TAG, "[delete]: delelte from " + this.tableName + " where "
                    + where.replace("?", String.valueOf(id)));
            rows =  db.delete(this.tableName, where, whereValue);
        } catch (Exception e) {
            DebugLog.d(TAG, "[delete] DB Exception.");
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
        return rows;
    }

    /**
     * 描述：按id删除.
     *
     * @param ids the ids
     * @return the int
     * @see IDBDao#delete(Integer...)
     */
    @Override
    public int delete(Integer... ids) {
        int rows = 0;
        if (ids.length > 0) {
            for (int i = 0; i < ids.length; i++) {
                rows += delete(ids[i]);
            }
        }
        return rows;
    }


    /**
     * 描述：按条件删除数据.
     *
     * @param whereClause the where clause
     * @param whereArgs the where args
     * @return the int
     * @see IDBDao#delete(String, String[])
     */
    @Override
    public int delete(String whereClause, String[] whereArgs) {
        int rows = 0;
        try {
            lock.lock();
            checkDBOpened();
            String mLogSql = getLogSql(whereClause,whereArgs);
            if(!TextUtils.isEmpty(mLogSql)){
                mLogSql +=" where ";
            }
            DebugLog.d(TAG, "[delete]: delete from " + this.tableName + mLogSql);
            rows = db.delete(this.tableName, whereClause, whereArgs);
        } catch (Exception e) {
            DebugLog.d(TAG, "[delete] DB Exception.");
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
        return rows;
    }

    /**
     * 描述：清空数据.
     *
     * @return the int 影响的行
     * @see IDBDao#
     */
    @Override
    public int deleteAll() {
        int rows = 0;
        try {
            lock.lock();
            checkDBOpened();
            DebugLog.d(TAG, "[delete]: delete from " + this.tableName);
            rows = db.delete(this.tableName,null,null);
        } catch (Exception e) {
            DebugLog.d(TAG, "[delete] DB Exception.");
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
        return rows;
    }

    /**
     * 描述：更新实体.
     * @param entity the entity
     * @return the int 影响的行
     * @see IDBDao#update(Object)
     */
    @Override
    public int update(T entity) {
        int rows = 0;
        try {
            lock.lock();
            checkDBOpened();
            ContentValues cv = new ContentValues();

            //注意返回的sql中包含主键列
            String sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT,METHOD_UPDATE);

            String where = this.idColumn + " = ?";
            int id = Integer.parseInt(cv.get(this.idColumn).toString());
            //set sql中不能包含主键列
            cv.remove(this.idColumn);

            DebugLog.d(TAG, "[update]: update " + this.tableName + " set " + sql
                    + " where " + where.replace("?", String.valueOf(id)));

            String[] whereValue = { Integer.toString(id) };
            rows = db.update(this.tableName, cv, where, whereValue);
        } catch (Exception e) {
            DebugLog.d(TAG, "[update] DB Exception.");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return rows;
    }

    @Override
    public int updateList(List<T> entityList) {
        String sql = null;
        int rows = 0;
        try {
            lock.lock();
            checkDBOpened();
            for(T entity:entityList){
                ContentValues cv = new ContentValues();

                sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT,
                        METHOD_UPDATE);

                String where = this.idColumn + " = ?";
                int id = Integer.parseInt(cv.get(this.idColumn).toString());
                cv.remove(this.idColumn);

                DebugLog.d(TAG, "[update]: update " + this.tableName + " set " + sql
                        + " where " + where.replace("?", String.valueOf(id)));

                String[] whereValue = { Integer.toString(id) };
                rows += db.update(this.tableName, cv, where, whereValue);
            }
        } catch (Exception e) {
            DebugLog.d(TAG, "[update] DB Exception.");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return rows;
    }

    /**
     * 查询数据库中的一条记录
     *
     * @param id the id
     * @return the t
     * @see IDBDao#queryOne(int)
     */
    @Override
    public T queryOne(int id) {
        synchronized (lock) {
            String selection = this.idColumn + " = ?";
            String[] selectionArgs = { Integer.toString(id) };
            DebugLog.d(TAG, "[queryOne]: select * from " + this.tableName + " where "
                    + this.idColumn + " = '" + id + "'");
            List<T> list = queryList(null, selection, selectionArgs, null, null, null,
                    null);
            if ((list != null) && (list.size() > 0)) {
                return (T) list.get(0);
            }
            return null;
        }
    }

    /**
     * 一种更灵活的方式查询，不支持对象关联，可以写完整的sql.
     *
     * @param sql 完整的sql如：select * from a ,b where a.id=b.id and a.id = ?
     * @param selectionArgs 绑定变量值
     * @param clazz  返回的对象类型
     * @return the list
     * @see IDBDao#rawQuery(String, String[], Class)
     */
    @Override
    public List<T> rawQuery(String sql, String[] selectionArgs,Class<T> clazz) {

        List<T> list = new ArrayList<T>();
        Cursor cursor = null;
        try {
            lock.lock();
            checkDBOpened();
            DebugLog.d(TAG, "[rawQuery]: " + getLogSql(sql, selectionArgs));
            cursor = db.rawQuery(sql, selectionArgs);
            getListFromCursor(clazz, list, cursor);
        } catch (Exception e) {
            DebugLog.e(TAG, "[rawQuery] from DB Exception.");
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
            lock.unlock();
        }

        return list;
    }

    /**
     * 判断某条记录是否存在
     *
     * @param sql the sql
     * @param selectionArgs the selection args
     * @return true, if is exist
     * @see IDBDao#isExist(String, String[])
     */
    @Override
    public boolean isExist(String sql, String[] selectionArgs) {
        Cursor cursor = null;
        try {
            lock.lock();
            checkDBOpened();
            DebugLog.d(TAG, "[isExist]: " + getLogSql(sql, selectionArgs));
            cursor = db.rawQuery(sql, selectionArgs);
            if (cursor.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            DebugLog.e(TAG, "[isExist] from DB Exception.");
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
            lock.unlock();
        }
        return false;
    }

    /**
     * 查询所有数据.
     *
     * @return the list
     * @see IDBDao#queryList()
     */
    @Override
    public List<T> queryList() {
        return queryList(null, null, null, null, null, null, null);
    }

    /**
     * 查询列表.
     *
     * @param columns the columns
     * @param selection the selection
     * @param selectionArgs the selection args
     * @param groupBy the group by
     * @param having the having
     * @param orderBy the order by
     * @param limit the limit
     * @return the list
     * @see IDBDao#queryList(String[], String, String[], String, String, String, String)
     */
    @Override
    public List<T> queryList(String[] columns, String selection,
                             String[] selectionArgs, String groupBy, String having,
                             String orderBy, String limit) {

        List<T> list = new ArrayList<T>();
        Cursor cursor = null;
        try {
            lock.lock();
            checkDBOpened();
            DebugLog.d(TAG, "[queryList] from " + this.tableName + " where " + selection + "(" + selectionArgs + ")" + " group by " + groupBy + " having " + having + " order by " + orderBy + " limit " + limit);
            cursor = db.query(this.tableName, columns, selection,
                    selectionArgs, groupBy, having, orderBy, limit);
            getListFromCursor(this.clazz, list, cursor);
        } catch (Exception e) {
            DebugLog.e(TAG, "[queryList] from DB Exception");
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
            lock.unlock();
        }

        return list;
    }

    /**
     *  简单一些的查询.
     *
     * @param selection the selection
     * @param selectionArgs the selection args
     * @return the list
     * @see IDBDao#queryCount(String, String[])
     *
     */
    @Override
    public List<T> queryList(String selection, String[] selectionArgs) {
        return queryList(null, selection,selectionArgs, null, null,null, null);
    }

    /**
     * 描述：查询为map列表.
     *
     * @param sql the sql
     * @param selectionArgs the selection args
     * @return the list
     * @see IDBDao#queryMapList(String, String[])
     */
    @Override
    public List<Map<String, String>> queryMapList(String sql,String[] selectionArgs) {
        Cursor cursor = null;
        List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
        try {
            lock.lock();
            checkDBOpened();
            DebugLog.d(TAG, "[queryMapList]: " + getLogSql(sql, selectionArgs));
            cursor = db.rawQuery(sql, selectionArgs);
            while (cursor.moveToNext()) {
                Map<String, String> map = new HashMap<String, String>();
                for (String columnName : cursor.getColumnNames()) {
                    int c = cursor.getColumnIndex(columnName);
                    if (c < 0) {
                        continue; // 如果不存在循环下个属性值
                    } else {
                        map.put(columnName.toLowerCase(), cursor.getString(c));
                    }
                }
                retList.add(map);
            }
        } catch (Exception e) {
            DebugLog.e(TAG, "[queryMapList] from DB exception");
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
            lock.unlock();
        }

        return retList;
    }

    /**
     * 描述：查询数量.
     * @param sql the sql
     * @param selectionArgs the selection args
     * @return the int
     * @see IDBDao#queryCount(String, String[])
     */
    @Override
    public int queryCount(String sql, String[] selectionArgs) {
        Cursor cursor = null;
        int count = 0;
        try{
            lock.lock();
            checkDBOpened();
            DebugLog.d(TAG, "[queryCount]: " + getLogSql(sql, selectionArgs));
            cursor = db.query(this.tableName, null, sql, selectionArgs, null, null,null);
            if(cursor != null){
                count = cursor.getCount();
            }
        }catch (Exception e){
            DebugLog.e(TAG, "[queryCount] from DB exception");
            e.printStackTrace();
        }finally{
            closeCursor(cursor);
            lock.unlock();
        }
        return count;
    }

    /**
     * 描述：执行特定的sql.
     *
     * @param sql the sql
     * @param selectionArgs the selection args
     * @see IDBDao#execSql(String, Object[])
     */
    @Override
    public void execSql(String sql, Object[] selectionArgs) {
        try {
            lock.lock();
            checkDBOpened();
            DebugLog.d(TAG, "[execSql]: " + getLogSql(sql, selectionArgs));
            if (selectionArgs == null) {
                db.execSQL(sql);
            } else {
                db.execSQL(sql, selectionArgs);
            }
        } catch (Exception e) {
            DebugLog.e(TAG, "[execSql] DB exception.");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 设置这个ContentValues.
     *
     * @param entity 映射实体
     * @param cv the cv
     * @param type id类的类型，是否自增
     * @param method 预执行的操作
     * @return sql的字符串
     * @throws IllegalAccessException the illegal access exception
     */
    private String setContentValues(T entity, ContentValues cv, int type,
                                    int method) throws IllegalAccessException {
        StringBuffer strField = new StringBuffer("(");
        StringBuffer strValue = new StringBuffer(" values(");
        StringBuffer strUpdate = new StringBuffer(" ");

        // 加载所有字段
        List<Field> allFields = TableHelper.joinFieldsOnlyColumn(entity.getClass().getDeclaredFields(),
                entity.getClass().getSuperclass().getDeclaredFields());
        for (Field field : allFields) {
            if (!field.isAnnotationPresent(Column.class)) {
                continue;
            }
            Column column = (Column) field.getAnnotation(Column.class);

            field.setAccessible(true);
            Object fieldValue = field.get(entity);
            if (fieldValue == null)
                continue;
            if ((type == TYPE_INCREMENT) && (field.isAnnotationPresent(Id.class))) {
                continue;
            }
            // 处理java.util.Date类型,update
            if (Date.class == field.getType()) {
                // 2012-06-10
                cv.put(column.name(), ((Date) fieldValue).getTime());
                continue;
            }
            String value = String.valueOf(fieldValue);
            cv.put(column.name(), value);
            if (method == METHOD_INSERT) {
                strField.append(column.name()).append(",");
                strValue.append("'").append(value).append("',");
            } else {
                strUpdate.append(column.name()).append("=").append("'").append(
                        value).append("',");
            }

        }
        if (method == METHOD_INSERT) {
            strField.deleteCharAt(strField.length() - 1).append(")");
            strValue.deleteCharAt(strValue.length() - 1).append(")");
            return strField.toString() + strValue.toString();
        } else {
            return strUpdate.deleteCharAt(strUpdate.length() - 1).append(" ").toString();
        }
    }

    /**
     * 从游标中获得映射对象列表.
     *
     * @param clazz the clazz
     * @param list 返回的映射对象列表
     * @param cursor 当前游标
     * @return the list from cursor
     * @throws IllegalAccessException the illegal access exception
     * @throws InstantiationException the instantiation exception
     */
    private void getListFromCursor(Class<?> clazz,List<T> list, Cursor cursor)
            throws IllegalAccessException, InstantiationException {
        while (cursor.moveToNext()) {
            Object entity = clazz.newInstance();
            // 加载所有字段
            List<Field> allFields = TableHelper.joinFieldsOnlyColumn(entity.getClass().getDeclaredFields(),
                    entity.getClass().getSuperclass().getDeclaredFields());


            for (Field field : allFields) {
                Column column = null;
                if (field.isAnnotationPresent(Column.class)) {
                    column = (Column) field.getAnnotation(Column.class);

                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();

                    int c = cursor.getColumnIndex(column.name());
                    if (c < 0) {
                        continue; // 如果不存则循环下个属性值
                    } else if ((Integer.TYPE == fieldType)
                            || (Integer.class == fieldType)) {
                        field.set(entity, cursor.getInt(c));
                    } else if (String.class == fieldType) {
                        field.set(entity, cursor.getString(c));
                    } else if ((Long.TYPE == fieldType)
                            || (Long.class == fieldType)) {
                        field.set(entity, Long.valueOf(cursor.getLong(c)));
                    } else if ((Float.TYPE == fieldType)
                            || (Float.class == fieldType)) {
                        field.set(entity, Float.valueOf(cursor.getFloat(c)));
                    } else if ((Short.TYPE == fieldType)
                            || (Short.class == fieldType)) {
                        field.set(entity, Short.valueOf(cursor.getShort(c)));
                    } else if ((Double.TYPE == fieldType)
                            || (Double.class == fieldType)) {
                        field.set(entity, Double.valueOf(cursor.getDouble(c)));
                    } else if (Date.class == fieldType) {// 处理java.util.Date类型,update2012-06-10
                        Date date = new Date();
                        date.setTime(cursor.getLong(c));
                        field.set(entity, date);
                    } else if (Blob.class == fieldType) {
                        field.set(entity, cursor.getBlob(c));
                    } else if (Character.TYPE == fieldType) {
                        String fieldValue = cursor.getString(c);
                        if ((fieldValue != null) && (fieldValue.length() > 0)) {
                            field.set(entity, Character.valueOf(fieldValue.charAt(0)));
                        }
                    }else if ((Boolean.TYPE == fieldType) || (Boolean.class == fieldType)) {
                        String temp = cursor.getString(c);
                        if ("true".equals(temp) || "1".equals(temp)){
                            field.set(entity, true);
                        }else{
                            field.set(entity, false);
                        }
                    }

                }
            }

            list.add((T) entity);
        }
    }

    /**
     * 描述：获取写数据库，数据操作前必须调用.
     *
     * @param transaction 是否开启事务
     */
    public void startWritableDatabase(boolean transaction){
        try {
            lock.lock();
            if(db == null || !db.isOpen()){
                db = this.dbHelper.getWritableDatabase();
            }
            if(db!=null && transaction){
                db.beginTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            lock.unlock();
        }

    }

    /**
     * 描述：获取读数据库，数据操作前必须调用.
     */
    public void startReadableDatabase(){
        try {
            lock.lock();
            if(db == null || !db.isOpen()){
                db = this.dbHelper.getReadableDatabase();
            }

        }catch (Exception e) {
            e.printStackTrace();
        }finally{
            lock.unlock();
        }

    }

    /**
     * 关闭数据库，数据操作后必须调用.
     */
    public void closeDatabase(){
        try {
            lock.lock();
            if(db!=null){
                if(db.inTransaction()){
                    db.setTransactionSuccessful();
                    db.endTransaction();
                }
                if(db.isOpen()){
                    db.close();
                }

            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
    }



    /**
     *
     * 检查DB是否已经打开.
     */
    private void checkDBOpened(){
        if(db == null){
            throw new RuntimeException("先调用 startReadableDatabase()或者startWritableDatabase(boolean transaction)初始化数据库。");
        }
    }

    /**
     * 打印当前sql语句.
     *
     * @param sql sql语句，带？
     * @param args 绑定变量
     * @return 完整的sql
     */
    private String getLogSql(String sql, Object[] args) {
        if (args == null || args.length == 0) {
            return sql;
        }
        for (int i = 0; i < args.length; i++) {
            sql = sql.replaceFirst("\\?", "'" + String.valueOf(args[i]) + "'");
        }
        return sql;
    }
}

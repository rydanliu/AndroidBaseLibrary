package com.tom.basecore.db;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.tom.basecore.db.annotation.Column;
import com.tom.basecore.db.annotation.Id;
import com.tom.basecore.db.annotation.Table;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:创建数据库表和删除数据库表工具类
 * User： yuanzeyao.
 * Date： 2015-09-02 18:03
 */
public class TableHelper {
    public static final String TAG="TableHelper";

    /**
     * 根据Class批量创建数据库表
     * @param db
     * @param clazzes
     */
    public static void createTableByClasses(SQLiteDatabase db,Class<?>[] clazzes){
        for(Class<?> clazz : clazzes){
            createTableByClass(db,clazz);
        }
    }

    /**
     * 根据Class创建一个具体的表
     * @param db
     * @param clazz
     */
    public static void createTableByClass(SQLiteDatabase db,Class<?> clazz){
        if(clazz==null){
            throw new NullPointerException("createTableByClass clazz is null!");
        }

        String tableName="";
        if(clazz.isAnnotationPresent(Table.class)){
            Table table=clazz.getAnnotation(Table.class);
            tableName=table.name();
        }

        if(TextUtils.isEmpty(tableName)){
            throw new IllegalArgumentException("table name is null!");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        List<Field> allFields = TableHelper.joinFieldsOnlyColumn(clazz.getDeclaredFields(), clazz.getSuperclass().getDeclaredFields());

        for (Field field : allFields) {
            if (!field.isAnnotationPresent(Column.class)) {
                continue;
            }

            Column column = (Column) field.getAnnotation(Column.class);

            String columnType = "";
            if (column.type().equals(""))
                columnType = getColumnType(field.getType());
            else {
                columnType = column.type();
            }

            sb.append(column.name() + " " + columnType);

            if (column.length() != 0) {
                sb.append("(" + column.length() + ")");
            }
            //实体类定义为Integer类型后不能生成Id异常
            if ((field.isAnnotationPresent(Id.class))
                    && ((field.getType() == Integer.TYPE) || (field.getType() == Integer.class)))
                sb.append(" primary key autoincrement");
            else if (field.isAnnotationPresent(Id.class)) {
                sb.append(" primary key");
            }

            sb.append(", ");
        }

        sb.delete(sb.length() - 2, sb.length() - 1);
        sb.append(")");

        String sql = sb.toString();

        Log.d(TAG, "create table [" + tableName + "]: " + sql);

        db.execSQL(sql);

    }

    /**
     * 根据Class 删除一个表
     * @param db
     * @param clazz
     * @param <T>
     */
    public static <T> void dropTable(SQLiteDatabase db, Class<T> clazz) {
        String tableName = "";
        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = (Table) clazz.getAnnotation(Table.class);
            tableName = table.name();
        }
        String sql = "DROP TABLE IF EXISTS " + tableName;
        Log.d(TAG, "dropTable[" + tableName + "]:" + sql);
        db.execSQL(sql);
    }

    /**
     * 合并Field数组并去重,并实现过滤掉非Column字段,和实现Id放在首字段位置功能.
     *
     * @param fields1 属性数组1
     * @param fields2 属性数组2
     * @return 属性的列表
     */
    public static List<Field> joinFieldsOnlyColumn(Field[] fields1, Field[] fields2) {
        Map<String, Field> map = new LinkedHashMap<String, Field>();
        for (Field field : fields1) {
            // 过滤掉非Column定义的字段
            if (!field.isAnnotationPresent(Column.class)) {
                continue;
            }
            Column column = (Column) field.getAnnotation(Column.class);
            map.put(column.name(), field);
        }
        for (Field field : fields2) {
            // 过滤掉非Column定义的字段
            if (!field.isAnnotationPresent(Column.class)) {
                continue;
            }
            Column column = (Column) field.getAnnotation(Column.class);
            if (!map.containsKey(column.name())) {
                map.put(column.name(), field);
            }
        }
        List<Field> list = new ArrayList<Field>();
        for (String key : map.keySet()) {
            Field tempField = map.get(key);
            // 如果是Id则放在首位置.
            if (tempField.isAnnotationPresent(Id.class)) {
                list.add(0, tempField);
            } else {
                list.add(tempField);
            }
        }
        return list;
    }

    /**
     * 获取列类型.
     *
     * @param fieldType the field type
     * @return 列类型
     */
    private static String getColumnType(Class<?> fieldType) {
        if (String.class == fieldType) {
            return "TEXT";
        }
        if ((Integer.TYPE == fieldType) || (Integer.class == fieldType)) {
            return "INTEGER";
        }
        if ((Long.TYPE == fieldType) || (Long.class == fieldType)) {
            return "BIGINT";
        }
        if ((Float.TYPE == fieldType) || (Float.class == fieldType)) {
            return "FLOAT";
        }
        if ((Short.TYPE == fieldType) || (Short.class == fieldType)) {
            return "INT";
        }
        if ((Double.TYPE == fieldType) || (Double.class == fieldType)) {
            return "DOUBLE";
        }
        if (Blob.class == fieldType) {
            return "BLOB";
        }

        return "TEXT";
    }
}

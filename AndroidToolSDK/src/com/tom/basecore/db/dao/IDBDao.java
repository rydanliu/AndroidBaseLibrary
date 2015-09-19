package com.tom.basecore.db.dao;

import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;
import java.util.Map;

/**
 * Description:
 * User�� yuanzeyao.
 * Date�� 2015-09-17 16:41
 */
public interface IDBDao<T> {
    /**
     * ��ȡ���ݿ�.
     *
     * @return the db helper
     */
    public SQLiteOpenHelper getDbHelper();

    /**
     * ����ʵ����,Ĭ����������,����insert(T,true);.
     *
     * @param entity ӳ��ʵ��
     * @return ����ɹ�������ID
     */
    public abstract long insert(T entity);

    /**
     * ����ʵ����.
     *
     * @param entity ӳ��ʵ��
     * @param flag flagΪtrue���Զ���������,flagΪfalseʱ���ֹ�ָ��������ֵ.
     * @return ����ɹ��������к�
     */
    public abstract long insert(T entity, boolean flag);

    /**
     * ����ʵ�����б�Ĭ����������,����insertList(List<T>,true);.
     *
     * @param entityList ӳ��ʵ���б�
     * @return ����ɹ��������к�
     */
    public abstract long[]  insertList(List<T> entityList);

    /**
     * ����ʵ�����б�.
     *
     * @param entityList ӳ��ʵ���б�
     * @param flag flagΪtrue���Զ���������,flagΪfalseʱ���ֹ�ָ��������ֵ
     * @return ����ɹ��������к�
     */
    public abstract long[] insertList(List<T> entityList, boolean flag);



    /**
     * ����IDɾ������.
     *
     * @param id ����ID����
     * @return the int Ӱ�������
     */
    public abstract int delete(int id);

    /**
     * ����IDɾ�����ݣ������.
     *
     * @param ids ����ID����
     * @return the int Ӱ�������
     */
    public abstract int delete(Integer... ids);

    /**
     * ����whereɾ������.
     *
     * @param whereClause where���
     * @param whereArgs  where����
     * @return the int Ӱ�������
     */
    public abstract int delete(String whereClause, String[] whereArgs);

    /**
     * ɾ����������.
     *
     * @return the int Ӱ�������
     */
    public abstract int deleteAll();

    /**
     * ��������.
     *
     * @param entity ����,ID����
     * @return Ӱ�������
     */
    public abstract int update(T entity);

    /**
     * ��������.
     *
     * @param entityList �����б�,ID����
     * @return Ӱ�������
     */
    public abstract int updateList(List<T> entityList);

    /**
     * ���ݻ�ȡһ������.
     *
     * @param id ����ID����
     * @return һ������ӳ��ʵ��
     */
    public abstract T queryOne(int id);

    /**
     * ִ�в�ѯ���.
     *
     * @param sql sql���
     * @param selectionArgs �󶨱����Ĳ���ֵ
     * @param clazz  ���صĶ�������
     * @return ӳ��ʵ���б�
     */
    public abstract List<T> rawQuery(String sql, String[] selectionArgs,Class<T> clazz);

    /**
     * ��ѯ�б�.
     *
     * @return ӳ��ʵ���б�
     */
    public abstract List<T> queryList();

    /**
     * ӳ��ʵ���б�.
     *
     * @param columns ��ѯ����
     * @param selection where����sql
     * @param selectionArgs where����sql�İ󶨱����Ĳ���
     * @param groupBy �������
     * @param having �����Ĺ������
     * @param orderBy ����
     * @param limit limit���
     * @return ӳ��ʵ���б�
     */
    public abstract List<T> queryList(String[] columns, String selection,
                                      String[] selectionArgs, String groupBy, String having,
                                      String orderBy, String limit);

    /**
     * ӳ��ʵ���б�.
     * @param selection where����sql
     * @param selectionArgs where����sql�İ󶨱����Ĳ���
     * @return ӳ��ʵ���б�
     */
    public abstract List<T> queryList(String selection,String[] selectionArgs);

    /**
     * ����Ƿ��������.
     *
     * @param sql sql���
     * @param selectionArgs �󶨱����Ĳ���ֵ
     * @return ������ڷ���true, ������Ϊfalse
     */
    public abstract boolean isExist(String sql, String[] selectionArgs);

    /**
     * ����ѯ�Ľ������Ϊ��ֵ��map.
     *
     * @param sql ��ѯsql
     * @param selectionArgs �󶨱����Ĳ���ֵ
     * @return ���ص�Map�е�keyȫ����Сд��ʽ.
     */
    public List<Map<String, String>> queryMapList(String sql,String[] selectionArgs);

    /**
     * ����һ����ѯ�Ľ������.
     * @param sql ��ѯsql
     * @param selectionArgs �󶨱����Ĳ���ֵ
     * @return ������.
     */
    public int queryCount(String sql,String[] selectionArgs);

    /**
     * ��װִ��sql����.
     *
     * @param sql sql���
     * @param selectionArgs �󶨱����Ĳ���ֵ
     */
    public void execSql(String sql, Object[] selectionArgs);
}

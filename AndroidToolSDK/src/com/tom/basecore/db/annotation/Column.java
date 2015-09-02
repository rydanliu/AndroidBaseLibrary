package com.tom.basecore.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:ͨ����ע�⣬��ʾĳһ���ֶδ������ݿ��һ��
 * User�� yuanzeyao.
 * Date�� 2015-09-02 17:50
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * @return �������ݿ�������
     */
    public abstract String name();
    /**
     * @return �������ݿ�������
     */
    public abstract String type();
    /**
     * @return �������ݿ��г���
     */
    public abstract int length();
}

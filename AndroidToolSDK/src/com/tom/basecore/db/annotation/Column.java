package com.tom.basecore.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:通过此注解，表示某一个字段代表数据库的一列
 * User： yuanzeyao.
 * Date： 2015-09-02 17:50
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * @return 返回数据库列名称
     */
    public abstract String name();
    /**
     * @return 返回数据库列类型
     */
    public abstract String type();
    /**
     * @return 返回数据库列长度
     */
    public abstract int length();
}

package com.tom.basecore.db.annotation;

/**
 * Description:通过此注解，表示一个类代表一个数据库
 * User： yuanzeyao.
 * Date： 2015-09-02 18:00
 */
public @interface Table {
    /**
     * @return 返回数据库名称
     */
    public abstract String name();
}

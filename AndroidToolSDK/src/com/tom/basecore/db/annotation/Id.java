package com.tom.basecore.db.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:��ʾ������һ��������
 * User�� yuanzeyao.
 * Date�� 2015-09-02 18:26
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { java.lang.annotation.ElementType.FIELD })
public @interface Id {
}

package com.bsw.dblibrary.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据持有类中的必填项
 *
 * @author bsw
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Require {
    /**
     * 别名设置
     *
     * @return 别名
     */
    String name() default "";
}

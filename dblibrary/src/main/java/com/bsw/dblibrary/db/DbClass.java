package com.bsw.dblibrary.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据持有类
 *
 * @author bsw
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DbClass {
    /**
     * 主键别名设置
     *
     * @return 别名
     */
    public String name() default "";
}

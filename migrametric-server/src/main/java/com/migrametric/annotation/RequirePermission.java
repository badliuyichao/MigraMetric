package com.migrametric.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 *
 * @author MigraMetric Team
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 权限编码
     */
    String[] value() default {};

    /**
     * 逻辑运算：AND-所有权限都满足，OR-满足任一权限
     */
    String logic() default "OR";
}

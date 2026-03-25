package com.migrametric.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标注在Controller方法上，自动记录操作日志
 *
 * @author MigraMetric Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作类型
     */
    String operationType() default "";

    /**
     * 操作描述
     */
    String description() default "";
}

package me.renzheng.beaker.service.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheEvict {

    /**
     * 缓存名称
     */
    String value() default "";

    /**
     * 缓存 key，支持 SpEL 表达式
     */
    String key() default "";

    /**
     * 是否清空所有缓存
     */
    boolean allEntries() default false;

    /**
     * 条件，支持 SpEL 表达式
     */
    String condition() default "";

    /**
     * 是否在方法执行前清除缓存
     */
    boolean beforeInvocation() default false;

}

package me.renzheng.beaker.service.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cacheable {

    /**
     * 缓存名称
     */
    String value() default "";

    /**
     * 缓存 key，支持 SpEL 表达式
     */
    String key() default "";

    /**
     * 缓存过期时间
     */
    long timeout() default -1;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 条件，支持 SpEL 表达式
     */
    String condition() default "";

    /**
     * 是否缓存 null 值
     */
    boolean cacheNull() default false;
}

package com.siyuan.springredis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import org.springframework.core.annotation.AliasFor;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SpringRedisValueCache {
	
	@AliasFor("key")
	String value() default "";
	
	String redisTemplate() default "";
	
	// 注解生效条件,支持SpringEL
	String condition() default "";
	
	// TTL <=0 永不过期
	long timeout() default 0;
	
	// TTL时间单位
	TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
	
	// key,支持SpringEL
	@AliasFor("value")
	String key() default "";
	
	// 缓存命中时是否刷新TTL
	boolean refreshTTL() default false;
	
}

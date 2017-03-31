package com.siyuan.springredis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SpringRedisValueEvict {
	
	@AliasFor("key")
	String value() default "";
	
	String redisTemplate() default "";
	
	// 注解生效条件,支持SpringEL
	String condition() default "";
	
	// key,支持SpringEL
	@AliasFor("value")
	String key() default "";
	
}

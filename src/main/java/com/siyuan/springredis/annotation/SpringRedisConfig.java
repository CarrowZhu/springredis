package com.siyuan.springredis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SpringRedisConfig {
	
	@AliasFor("redisTemplate")
	String value() default "";
	
	@AliasFor("value") // 必须互为@AliasFor
	String redisTemplate() default "";
	
}

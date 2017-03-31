package com.siyuan.springredis.annotation;

import java.lang.reflect.Method;
import java.util.List;

import com.siyuan.springredis.operation.SpringRedisOperation;

/**
 * 注解解析器
 */
public interface SpringRedisAnnotationParser {
	
	List<SpringRedisOperation> parseSpringRedisOperations(Method method, Class<?> targetClass);
	
}

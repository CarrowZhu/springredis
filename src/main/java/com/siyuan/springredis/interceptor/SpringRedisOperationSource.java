package com.siyuan.springredis.interceptor;

import java.lang.reflect.Method;
import java.util.List;

import com.siyuan.springredis.operation.SpringRedisOperation;

public interface SpringRedisOperationSource {
	
	List<SpringRedisOperation> getSpringRedisOperations(Method method, Class<?> targetClass);
	
}

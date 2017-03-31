package com.siyuan.springredis.interceptor;

import java.lang.reflect.Method;

import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.util.CollectionUtils;

public abstract class SpringRedisOperationSourcePointcut extends StaticMethodMatcherPointcut {
	
	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		SpringRedisOperationSource operationSource = getSpringRedisOperationSource();
		return (operationSource != null 
				&& !CollectionUtils.isEmpty(operationSource.getSpringRedisOperations(method, targetClass)));
	}
	
	protected abstract SpringRedisOperationSource getSpringRedisOperationSource();
	
}

package com.siyuan.springredis.interceptor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.siyuan.springredis.annotation.DefaultSpringRedisAnnotationParser;
import com.siyuan.springredis.annotation.SpringRedisAnnotationParser;
import com.siyuan.springredis.operation.SpringRedisOperation;

/**
 * 从注解中获取SpringRedisOperation
 */
public class AnnotationSpringRedisOperationSource implements SpringRedisOperationSource {
	
	private static final Log LOGGER = LogFactory.getLog(AnnotationSpringRedisOperationSource.class);
	
	// 默认值
	private String redisTemplate;
	
	private final SpringRedisAnnotationParser annotationParser = new DefaultSpringRedisAnnotationParser();
	
	// 缓存
	private final Map<AnnotatedElementKey, List<SpringRedisOperation>> operationCache = 
			new ConcurrentHashMap<AnnotatedElementKey, List<SpringRedisOperation>>(1024);
	
	// 无注解方法在attributeCache中对应的值
	private final static List<SpringRedisOperation> NULL_CACHING_ATTRIBUTE = Collections.emptyList();
	
	@Override
	public List<SpringRedisOperation> getSpringRedisOperations(Method method, Class<?> targetClass) {
		AnnotatedElementKey cacheKey = new AnnotatedElementKey(method, targetClass);
		List<SpringRedisOperation> operationsCached = operationCache.get(cacheKey);
		if (operationsCached != null) {
			return operationsCached;
		}
		
		List<SpringRedisOperation> operations = annotationParser.parseSpringRedisOperations(method, targetClass);
		if (CollectionUtils.isEmpty(operations)) {
			operationCache.put(cacheKey, NULL_CACHING_ATTRIBUTE);
		} else {
			for (SpringRedisOperation operation : operations) {
				applyDefault(operation);
			}
			operationCache.put(cacheKey, operations);
			LOGGER.debug("[SpringRedis][OperationSource]-cache key[class=" + targetClass.getName() 
					+ ", method=" + method.getName() + "] with value[" + operations + "]");
		}
		return operations;
	}
	
	private void applyDefault(SpringRedisOperation operation) {
		if (!StringUtils.hasText(operation.getRedisTemplate()) && StringUtils.hasText(this.redisTemplate)) {
			operation.setRedisTemplate(this.redisTemplate);
		}
	}
	
	public String getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(String redisTemplate) {
		Assert.hasText(redisTemplate, "[redisTemplate] must have text; it must not be null, empty, or blank");
		this.redisTemplate = redisTemplate;
	}
	
}

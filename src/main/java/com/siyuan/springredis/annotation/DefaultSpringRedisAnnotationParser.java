package com.siyuan.springredis.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import com.siyuan.springredis.operation.SpringRedisHashCacheOperation;
import com.siyuan.springredis.operation.SpringRedisHashEvictOperation;
import com.siyuan.springredis.operation.SpringRedisOperation;
import com.siyuan.springredis.operation.SpringRedisValueCacheOperation;
import com.siyuan.springredis.operation.SpringRedisValueEvictOperation;

public class DefaultSpringRedisAnnotationParser implements SpringRedisAnnotationParser {

	@Override
	public List<SpringRedisOperation> parseSpringRedisOperations(Method method, Class<?> targetClass) {
		List<SpringRedisOperation> operations = new LinkedList<SpringRedisOperation>();
		
		DefaultCacheConfig defaultConfig = getDefaultCacheConfig(targetClass);
		
		List<SpringRedisValueCache> valueCaches = getAnnotations(method, SpringRedisValueCache.class);
		for (SpringRedisValueCache valueCache : valueCaches) {
			operations.add(defaultConfig.applyDefault(parseValueCacheAnnotation(valueCache)));
		}
		
		List<SpringRedisHashCache> hashCaches = getAnnotations(method, SpringRedisHashCache.class);
		for (SpringRedisHashCache hashCache : hashCaches) {
			operations.add(defaultConfig.applyDefault(parseHashCacheAnnotation(hashCache)));
		}
		
		List<SpringRedisValueEvict> valueEvicts = getAnnotations(method, SpringRedisValueEvict.class);
		for (SpringRedisValueEvict valueEvict : valueEvicts) {
			operations.add(defaultConfig.applyDefault(parseValueEvictAnnotation(valueEvict)));
		}
		
		List<SpringRedisHashEvict> hashEvicts = getAnnotations(method, SpringRedisHashEvict.class);
		for (SpringRedisHashEvict hashEvict : hashEvicts) {
			operations.add(defaultConfig.applyDefault(parseHashEvictAnnotation(hashEvict)));
		}
		
		return operations;
	}
	
	/**
	 * {@link com.siyuan.springredis.annotation.SpringRedisValueCache}
	 */
	private SpringRedisOperation parseValueCacheAnnotation(SpringRedisValueCache valueCache) {
		SpringRedisValueCacheOperation operation = new SpringRedisValueCacheOperation();
		operation.setRedisTemplate(valueCache.redisTemplate());
		operation.setCondition(valueCache.condition());
		operation.setTimeout(valueCache.timeout());
		operation.setTimeUnit(valueCache.timeUnit());
		operation.setKey(valueCache.key());
		operation.setRefreshTTL(valueCache.refreshTTL());
		return operation;
	}
	
	/**
	 * {@link com.siyuan.springredis.annotation.SpringRedisHashCache}
	 */
	private SpringRedisOperation parseHashCacheAnnotation(SpringRedisHashCache hashCache) {
		SpringRedisHashCacheOperation operation = new SpringRedisHashCacheOperation();
		operation.setRedisTemplate(hashCache.redisTemplate());
		operation.setCondition(hashCache.condition());
		operation.setTimeout(hashCache.timeout());
		operation.setTimeUnit(hashCache.timeUnit());
		operation.setKey(hashCache.key());
		operation.setRefreshTTL(hashCache.refreshTTL());
		operation.setHashKey(hashCache.hashKey());
		return operation;
	}
	
	/**
	 * {@link com.siyuan.springredis.annotation.SpringRedisValueEvict}
	 */
	private SpringRedisOperation parseValueEvictAnnotation(SpringRedisValueEvict valueEvict) {
		SpringRedisValueEvictOperation operation = new SpringRedisValueEvictOperation();
		operation.setRedisTemplate(valueEvict.redisTemplate());
		operation.setCondition(valueEvict.condition());
		operation.setKey(valueEvict.key());
		return operation;
	}
	
	/**
	 * {@link com.siyuan.springredis.annotation.SpringRedisHashEvict}
	 */
	private SpringRedisOperation parseHashEvictAnnotation(SpringRedisHashEvict hashEvict) {
		SpringRedisHashEvictOperation operation = new SpringRedisHashEvictOperation();
		operation.setRedisTemplate(hashEvict.redisTemplate());
		operation.setCondition(hashEvict.condition());
		operation.setKey(hashEvict.key());
		operation.setHashKey(hashEvict.hashKey());
		return operation;
	}

	/**
	 * {@link com.siyuan.springredis.annotation.SpringRedisConfig}
	 */
	private DefaultCacheConfig getDefaultCacheConfig(Class<?> target) {
		SpringRedisConfig annotation = AnnotationUtils.getAnnotation(target, SpringRedisConfig.class);
		if (annotation != null) {
			return new DefaultCacheConfig(annotation.redisTemplate());
		}
		return new DefaultCacheConfig(null);
	}
	
	private <A extends Annotation> List<A> getAnnotations(AnnotatedElement ae, Class<A> annotationType) {
		List<A> anns = new LinkedList<A>();

		// look at raw annotation
		A ann = ae.getAnnotation(annotationType);
		if (ann != null) {
			anns.add(AnnotationUtils.synthesizeAnnotation(ann, ae));
		}

		// scan meta-annotations
		for (Annotation metaAnn : ae.getAnnotations()) {
			ann = metaAnn.annotationType().getAnnotation(annotationType);
			if (ann != null) {
				anns.add(AnnotationUtils.synthesizeAnnotation(ann, ae));
			}
		}

		return anns;
	}
	
	class DefaultCacheConfig {
		
		private String redisTemplate;
		
		private DefaultCacheConfig() {
		}
		
		private DefaultCacheConfig(String redisTemplate) {
			this.redisTemplate = redisTemplate;
		}
		
		public SpringRedisOperation applyDefault(SpringRedisOperation operation) {
			if (!StringUtils.hasText(operation.getRedisTemplate()) && StringUtils.hasText(this.redisTemplate)) {
				operation.setRedisTemplate(this.redisTemplate);
			}
			return operation;
		}
		
	}
	
}

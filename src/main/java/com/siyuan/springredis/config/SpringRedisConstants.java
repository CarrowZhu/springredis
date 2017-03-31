package com.siyuan.springredis.config;

/**
 * 相关常量
 */
public interface SpringRedisConstants {
	
	String SPRINGREDIS_ADVISOR_BEAN_NAME = "com.siyuan.springredis.interceptor.SpringRedisOperationSourceAdvisor";
	
	String ATTR_ANNOTATIONDRIVEN_REDISTEMPLATE = "redisTemplate";
	
	String ATTR_ANNOTATIONDRIVEN_ORDER = "order";
	
	String ATTR_ANNOTATIONDRIVEN_EXCEPTIONHANDLER = "exceptionHandler";
	
}

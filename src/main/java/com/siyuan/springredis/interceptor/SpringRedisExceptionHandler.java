package com.siyuan.springredis.interceptor;

/**
 * Redis操作过程的异常处理器
 */
public interface SpringRedisExceptionHandler {
	
	void handleFindCacheException(Exception e, Object...keys); 
	
	void handleCacheException(Exception e, Object result, Object...keys); 
	
	void handleRefreshTTLException(Exception e, Object result, Object...keys); 
	
	void handleEvictException(Exception e, Object result, Object...keys); 
	
}

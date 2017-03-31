package com.siyuan.springredis.interceptor;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 将异常信息记录到LOGGER中
 */
public class LoggerExceptionHandler implements SpringRedisExceptionHandler {
	
	private static final Log LOGGER = LogFactory.getLog(LoggerExceptionHandler.class);
	
	@Override
	public void handleFindCacheException(Exception e, Object... keys) {
		LOGGER.error("从key" + Arrays.toString(keys) + "中获取数据异常", e);
	}

	@Override
	public void handleCacheException(Exception e, Object result, Object... keys) {
		LOGGER.error("缓存数据到key" + Arrays.toString(keys) + "时异常", e);
	}

	@Override
	public void handleRefreshTTLException(Exception e, Object result, Object... keys) {
		LOGGER.error("刷新key" + Arrays.toString(keys) + "的存活时间异常", e);
	}

	@Override
	public void handleEvictException(Exception e, Object result, Object... keys) {
		LOGGER.error("从key" + Arrays.toString(keys) + "中清除数据异常", e);
	}

}

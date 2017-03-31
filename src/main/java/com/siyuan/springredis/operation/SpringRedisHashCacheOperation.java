package com.siyuan.springredis.operation;

import java.util.concurrent.TimeUnit;

import org.springframework.util.Assert;

public class SpringRedisHashCacheOperation extends SpringRedisOperation {
	
	private Long timeout;
	
	private TimeUnit timeUnit;
	
	private boolean refreshTTL;
	
	private String hashKey;
	
	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public boolean isRefreshTTL() {
		return refreshTTL;
	}

	public void setRefreshTTL(boolean refreshTTL) {
		this.refreshTTL = refreshTTL;
	}
	
	public String getHashKey() {
		return hashKey;
	}

	public void setHashKey(String hashKey) {
		Assert.hasText(hashKey, "[hashKey] must have text; it must not be null, empty, or blank");
		this.hashKey = hashKey;
	}

	@Override
	protected void appendChildDescription(StringBuilder result) {
		result.append(", timeout=").append(this.timeout);
		result.append(", timeUnit=").append(this.timeUnit);
		result.append(", refreshTTL=").append(this.refreshTTL);
		result.append(", hashKey=").append(this.hashKey);
	}
	
}

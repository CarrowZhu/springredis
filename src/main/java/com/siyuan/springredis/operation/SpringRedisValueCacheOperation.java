package com.siyuan.springredis.operation;

import java.util.concurrent.TimeUnit;

public class SpringRedisValueCacheOperation extends SpringRedisOperation {
	
	private Long timeout;
	
	private TimeUnit timeUnit;
	
	private boolean refreshTTL;
	
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
	
	@Override
	protected void appendChildDescription(StringBuilder result) {
		result.append(", timeout=").append(this.timeout);
		result.append(", timeUnit=").append(this.timeUnit);
		result.append(", refreshTTL=").append(this.refreshTTL);
	}
	
}

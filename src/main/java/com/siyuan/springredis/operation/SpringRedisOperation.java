package com.siyuan.springredis.operation;

import org.springframework.util.Assert;

public abstract class SpringRedisOperation {
	
	private String redisTemplate;
	
	private String condition;
	
	private String key;

	public String getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(String redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		Assert.hasText(key, "[key] must have text; it must not be null, empty, or blank");
		this.key = key;
	}
	
	@Override
	public boolean equals(Object other) {
		return (other instanceof SpringRedisOperation && toString().equals(other.toString()));
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return getOperationDescription().toString();
	}
	
	protected StringBuilder getOperationDescription() {
		StringBuilder result = new StringBuilder(getClass().getSimpleName());
		result.append("[redisTemplate=").append(this.redisTemplate);
		result.append(", condition=").append(this.condition);
		result.append(", key=").append(this.key);
		appendChildDescription(result);
		result.append("]");
		return result;
	}
	
	protected abstract void appendChildDescription(StringBuilder result);
	
}

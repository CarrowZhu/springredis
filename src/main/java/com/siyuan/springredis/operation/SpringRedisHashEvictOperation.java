package com.siyuan.springredis.operation;

import org.springframework.util.Assert;

public class SpringRedisHashEvictOperation extends SpringRedisOperation {
	
	private String hashKey;
	
	public String getHashKey() {
		return hashKey;
	}

	public void setHashKey(String hashKey) {
		Assert.hasText(hashKey, "[hashKey] must have text; it must not be null, empty, or blank");
		this.hashKey = hashKey;
	}
	
	@Override
	protected void appendChildDescription(StringBuilder result) {
		result.append(", hashKey=").append(this.hashKey);
	}
	
}

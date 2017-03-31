package com.siyuan.springredis.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * SpringRedis命名空间处理器
 */
public class SpringRedisNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("annotation-driven", new AnnotationDrivenSpringRedisBeanDefinitionParser());
	}

}

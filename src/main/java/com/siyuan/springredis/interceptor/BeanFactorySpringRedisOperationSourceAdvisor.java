package com.siyuan.springredis.interceptor;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

@SuppressWarnings("serial")
public class BeanFactorySpringRedisOperationSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor {
	
	private SpringRedisOperationSource operationSource;

	private final SpringRedisOperationSourcePointcut pointcut = new SpringRedisOperationSourcePointcut() {

		@Override
		protected SpringRedisOperationSource getSpringRedisOperationSource() {
			return operationSource;
		}
		
	};
	
	@Override
	public Pointcut getPointcut() {
		return pointcut;
	}

	public void setOperationSource(SpringRedisOperationSource operationSource) {
		this.operationSource = operationSource;
	}
	
}

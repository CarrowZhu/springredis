package com.siyuan.springredis.interceptor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.siyuan.springredis.expression.SpringRedisExpressionEvaluator;
import com.siyuan.springredis.operation.SpringRedisHashCacheOperation;
import com.siyuan.springredis.operation.SpringRedisHashEvictOperation;
import com.siyuan.springredis.operation.SpringRedisOperation;
import com.siyuan.springredis.operation.SpringRedisValueCacheOperation;
import com.siyuan.springredis.operation.SpringRedisValueEvictOperation;

public class SpringRedisInterceptor implements MethodInterceptor, ApplicationContextAware {

	private ApplicationContext applicationContext;

	private SpringRedisOperationSource operationSource;
	
	private SpringRedisExceptionHandler exceptionHandler = new LoggerExceptionHandler();
	
	private final SpringRedisExpressionEvaluator evaluator = new SpringRedisExpressionEvaluator();

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object target = invocation.getThis();
		Method method = invocation.getMethod();
		Object[] args = invocation.getArguments();

		Class<?> targetClass = getTargetClass(target);
		List<SpringRedisOperation> operations = operationSource.getSpringRedisOperations(method, targetClass);
		// 不存在注解信息则直接调用原方法返回
		if (CollectionUtils.isEmpty(operations)) {
			return invocation.proceed();
		}
		return execute(invocation, new SpringRedisOperationContexts(operations, method, args, target, targetClass));
	}

	private Object execute(MethodInvocation invocation, SpringRedisOperationContexts operationContexts)
			throws Throwable {
		// 从缓存中查找数据
		Object cachedItem = findCache(operationContexts);
		Object result = cachedItem;
		
		// 缓存miss
		if (cachedItem == null) { 
			result = invocation.proceed();
		}
		
		// 缓存数据
		if (cachedItem == null && result != null) {
			cache(operationContexts, result);
		// 刷新TTL
		} else if (cachedItem != null) {
			refreshTTL(operationContexts, result);
		}
		
		// 清除缓存
		evict(operationContexts, result);

		return result;
	}

	private Object findCache(SpringRedisOperationContexts operationContexts) {
		List<SpringRedisOperationContext> operationCtxts = operationContexts.get(SpringRedisValueCacheOperation.class);
		for (SpringRedisOperationContext operationContext : operationCtxts) {
			if (operationContext.isConditionPassing(SpringRedisExpressionEvaluator.NO_RESULT)) {
				Object key = operationContext.generateKey(SpringRedisExpressionEvaluator.NO_RESULT);
				try {
					Object cachedItem = operationContext.getRedisTemplate().opsForValue().get(key);
					if (cachedItem != null) {
						return cachedItem;
					}
				} catch (Exception e) {
					exceptionHandler.handleFindCacheException(e, key);
				}
			}
		}

		operationCtxts = operationContexts.get(SpringRedisHashCacheOperation.class);
		for (SpringRedisOperationContext operationContext : operationCtxts) {
			if (operationContext.isConditionPassing(SpringRedisExpressionEvaluator.NO_RESULT)) {
				Object key = operationContext.generateKey(SpringRedisExpressionEvaluator.NO_RESULT);
				Object hashKey = operationContext.generateHashKey(SpringRedisExpressionEvaluator.NO_RESULT);
				try {
					Object cachedItem = operationContext.getRedisTemplate().opsForHash().get(key, hashKey);
					if (cachedItem != null) {
						return cachedItem;
					}
				} catch (Exception e) {
					exceptionHandler.handleFindCacheException(e, key, hashKey);
				}
			}
		}

		return null;
	}

	private void cache(SpringRedisOperationContexts operationContexts, Object result) {
		List<SpringRedisOperationContext> operationCtxts = operationContexts.get(SpringRedisValueCacheOperation.class);
		for (SpringRedisOperationContext operationContext : operationCtxts) {
			SpringRedisValueCacheOperation operation = (SpringRedisValueCacheOperation) operationContext.operation;
			if (operationContext.isConditionPassing(result)) {
				Object key = operationContext.generateKey(result);
				try {
					if (operation.getTimeout() > 0) {
						operationContext.getRedisTemplate().opsForValue()
							.set(key, result, operation.getTimeout(), operation.getTimeUnit());
					} else {
						operationContext.getRedisTemplate().opsForValue().set(key, result);
					}
				} catch (Exception e) {
					exceptionHandler.handleCacheException(e, result, key);
				}
			}
		}

		operationCtxts = operationContexts.get(SpringRedisHashCacheOperation.class);
		for (SpringRedisOperationContext operationContext : operationCtxts) {
			SpringRedisHashCacheOperation operation = (SpringRedisHashCacheOperation) operationContext.operation;
			if (operationContext.isConditionPassing(result)) {
				Object key = operationContext.generateKey(result);
				Object hashKey = operationContext.generateHashKey(result);
				try {
					operationContext.getRedisTemplate().opsForHash().put(key, hashKey, result);
					if (operation.getTimeout() > 0) {
						operationContext.getRedisTemplate().expire(key, operation.getTimeout(), operation.getTimeUnit());
					}
				} catch (Exception e) {
					exceptionHandler.handleCacheException(e, result, key, hashKey);
				}
			}
		}
	}

	private void refreshTTL(SpringRedisOperationContexts operationContexts, Object result) {
		List<SpringRedisOperationContext> operationCtxts = operationContexts.get(SpringRedisValueCacheOperation.class);
		for (SpringRedisOperationContext operationContext : operationCtxts) {
			SpringRedisValueCacheOperation operation = (SpringRedisValueCacheOperation) operationContext.operation;
			if (operationContext.isConditionPassing(result) && operation.isRefreshTTL() && operation.getTimeout() > 0) {
				Object key = operationContext.generateKey(result);
				try {
					operationContext.getRedisTemplate().expire(key, operation.getTimeout(), operation.getTimeUnit());
				} catch (Exception e) {
					exceptionHandler.handleRefreshTTLException(e, result, key);
				}	
			}
		}

		operationCtxts = operationContexts.get(SpringRedisHashCacheOperation.class);
		for (SpringRedisOperationContext operationContext : operationCtxts) {
			SpringRedisHashCacheOperation operation = (SpringRedisHashCacheOperation) operationContext.operation;
			if (operationContext.isConditionPassing(result) && operation.isRefreshTTL() && operation.getTimeout() > 0) {
				Object key = operationContext.generateKey(result);
				try {
					operationContext.getRedisTemplate().expire(key, operation.getTimeout(), operation.getTimeUnit());
				} catch (Exception e) {
					exceptionHandler.handleRefreshTTLException(e, result, key);
				}
			}
		}
	}

	private void evict(SpringRedisOperationContexts operationContexts, Object result) {
		List<SpringRedisOperationContext> operationCtxts = operationContexts.get(SpringRedisValueEvictOperation.class);
		for (SpringRedisOperationContext operationContext : operationCtxts) {
			if (operationContext.isConditionPassing(result)) {
				Object key = operationContext.generateKey(result);
				try {
					operationContext.getRedisTemplate().delete(key);
				} catch (Exception e) {
					exceptionHandler.handleEvictException(e, result, key);
				}
			}
		}

		operationCtxts = operationContexts.get(SpringRedisHashEvictOperation.class);
		for (SpringRedisOperationContext operationContext : operationCtxts) {
			if (operationContext.isConditionPassing(result)) {
				Object key = operationContext.generateKey(result);
				Object hashKey = operationContext.generateHashKey(result);
				try {
					operationContext.getRedisTemplate().opsForHash().delete(key, hashKey);
				} catch (Exception e) {
					exceptionHandler.handleEvictException(e, result, key, hashKey);
				}
			}
		}
	}

	private Class<?> getTargetClass(Object target) {
		Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
		if (targetClass == null && target != null) {
			targetClass = target.getClass();
		}
		return targetClass;
	}

	private class SpringRedisOperationContexts {

		private final MultiValueMap<Class<? extends SpringRedisOperation>, SpringRedisOperationContext> contexts = 
				new LinkedMultiValueMap<Class<? extends SpringRedisOperation>, SpringRedisOperationContext>();

		public SpringRedisOperationContexts(List<? extends SpringRedisOperation> operations, Method method,
				Object[] args, Object target, Class<?> targetClass) {
			for (SpringRedisOperation operation : operations) {
				this.contexts.add(operation.getClass(), new SpringRedisOperationContext(operation, targetClass, method,
						target, args));
			}
		}

		public List<SpringRedisOperationContext> get(Class<? extends SpringRedisOperation> operationClass) {
			List<SpringRedisOperationContext> result = this.contexts.get(operationClass);
			return (result != null ? result : Collections.<SpringRedisOperationContext> emptyList());
		}
	}

	private class SpringRedisOperationContext {

		private SpringRedisOperation operation;

		private Class<?> targetClass;

		private Method method;

		private Object target;

		private Object[] args;

		private final AnnotatedElementKey methodCacheKey;

		public SpringRedisOperationContext(SpringRedisOperation operation, Class<?> targetClass, Method method,
				Object target, Object[] args) {
			this.operation = operation;
			this.targetClass = targetClass;
			this.method = method;
			this.target = target;
			this.args = extractArgs(method, args);
			this.methodCacheKey = new AnnotatedElementKey(method, targetClass);
		}

		private Object[] extractArgs(Method method, Object[] args) {
			if (!method.isVarArgs()) {
				return args;
			}
			Object[] varArgs = ObjectUtils.toObjectArray(args[args.length - 1]);
			Object[] combinedArgs = new Object[args.length - 1 + varArgs.length];
			System.arraycopy(args, 0, combinedArgs, 0, args.length - 1);
			System.arraycopy(varArgs, 0, combinedArgs, args.length - 1, varArgs.length);
			return combinedArgs;
		}

		protected boolean isConditionPassing(Object result) {
			if (StringUtils.hasText(this.operation.getCondition())) {
				EvaluationContext evaluationContext = createEvaluationContext(result);
				return evaluator.condition(this.operation.getCondition(), this.methodCacheKey, evaluationContext);
			}
			return true;
		}

		protected Object generateKey(Object result) {
			if (StringUtils.hasText(this.operation.getKey())) {
				EvaluationContext evaluationContext = createEvaluationContext(result);
				return evaluator.key(this.operation.getKey(), this.methodCacheKey, evaluationContext);
			}
			throw new IllegalStateException("[operation.key] must have text; it must not be null, empty, or blank");
		}

		protected Object generateHashKey(Object result) {
			String hashKey = null;
			if (this.operation instanceof SpringRedisHashCacheOperation) {
				hashKey = ((SpringRedisHashCacheOperation) this.operation).getHashKey();
			} else if (this.operation instanceof SpringRedisHashEvictOperation) {
				hashKey = ((SpringRedisHashEvictOperation) this.operation).getHashKey();
			} else {
				throw new UnsupportedOperationException(
						"[generateHashKey] is only supportted for SpringRedisHashCacheOperation or SpringRedisHashEvictOperation");
			}
			if (StringUtils.hasText(hashKey)) {
				EvaluationContext evaluationContext = createEvaluationContext(result);
				return evaluator.hashKey(hashKey, this.methodCacheKey, evaluationContext);
			}
			throw new IllegalStateException("[operation.hashKey] must have text; it must not be null, empty, or blank");
		}

		@SuppressWarnings("unchecked")
		protected <K, V> RedisTemplate<K, V> getRedisTemplate() {
			return applicationContext.getBean(operation.getRedisTemplate(), RedisTemplate.class);
		}

		private EvaluationContext createEvaluationContext(Object result) {
			return evaluator.createEvaluationContext(this.method, this.args, this.target, this.targetClass, result);
		}

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public void setOperationSource(SpringRedisOperationSource operationSource) {
		this.operationSource = operationSource;
	}

	public void setExceptionHandler(SpringRedisExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}
	
}

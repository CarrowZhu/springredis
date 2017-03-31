package com.siyuan.springredis.expression;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;

/**
 * copy from {@link org.springframework.cache.interceptor.ExpressionEvaluator}
 */
public class SpringRedisExpressionEvaluator extends CachedExpressionEvaluator {

	/**
	 * Indicate that there is no result variable.
	 */
	public static final Object NO_RESULT = new Object();

	/**
	 * The name of the variable holding the result object.
	 */
	public static final String RESULT_VARIABLE = "result";

	// shared param discoverer since it caches data internally
	private final ParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();

	private final Map<ExpressionKey, Expression> conditionCache = new ConcurrentHashMap<ExpressionKey, Expression>(64);

	private final Map<ExpressionKey, Expression> keyCache = new ConcurrentHashMap<ExpressionKey, Expression>(64);

	private final Map<ExpressionKey, Expression> hashKeyCache = new ConcurrentHashMap<ExpressionKey, Expression>(64);

	private final Map<AnnotatedElementKey, Method> targetMethodCache = new ConcurrentHashMap<AnnotatedElementKey, Method>(64);

	public EvaluationContext createEvaluationContext(Method method, Object[] args, Object target, Class<?> targetClass) {
		return createEvaluationContext(method, args, target, targetClass, NO_RESULT);
	}

	public EvaluationContext createEvaluationContext(Method method, Object[] args, Object target, Class<?> targetClass,
			Object result) {
		SpringRedisExpressionRootObject rootObject = new SpringRedisExpressionRootObject(method, args, target,
				targetClass);
		Method targetMethod = getTargetMethod(targetClass, method);
		MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(rootObject, targetMethod,
				args, this.paramNameDiscoverer);
		if (result != NO_RESULT) {
			evaluationContext.setVariable(RESULT_VARIABLE, result);
		}
		return evaluationContext;
	}

	public boolean condition(String conditionExpression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
		return getExpression(this.conditionCache, methodKey, conditionExpression).getValue(evalContext, boolean.class);
	}

	public Object key(String keyExpression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
		return getExpression(this.keyCache, methodKey, keyExpression).getValue(evalContext);
	}

	public Object hashKey(String hashKeyExpression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
		return getExpression(this.hashKeyCache, methodKey, hashKeyExpression).getValue(evalContext);
	}

	private Method getTargetMethod(Class<?> targetClass, Method method) {
		AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
		Method targetMethod = this.targetMethodCache.get(methodKey);
		if (targetMethod == null) {
			targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
			if (targetMethod == null) {
				targetMethod = method;
			}
			this.targetMethodCache.put(methodKey, targetMethod);
		}
		return targetMethod;
	}

}

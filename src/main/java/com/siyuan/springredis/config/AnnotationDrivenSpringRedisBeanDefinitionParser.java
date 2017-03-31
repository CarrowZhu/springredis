package com.siyuan.springredis.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.siyuan.springredis.interceptor.AnnotationSpringRedisOperationSource;
import com.siyuan.springredis.interceptor.BeanFactorySpringRedisOperationSourceAdvisor;
import com.siyuan.springredis.interceptor.SpringRedisInterceptor;

/**
 * annotation-driven元素解析器
 */
public class AnnotationDrivenSpringRedisBeanDefinitionParser implements BeanDefinitionParser {

	private static final Log LOGGER = LogFactory.getLog(AnnotationDrivenSpringRedisBeanDefinitionParser.class);

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		// 确保Spring容器中有AdvisorAutoProxyCreator对象
		AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);
		// 避免重复注册SpringRedisOperationSourceAdvisor
		if (!parserContext.getRegistry().containsBeanDefinition(SpringRedisConstants.SPRINGREDIS_ADVISOR_BEAN_NAME)) {
			LOGGER.info("[SpringRedis][AnnotationDriven]-init-start");
			Object eleSource = parserContext.extractSource(element);

			// OperationSource
			RootBeanDefinition sourceDef = new RootBeanDefinition(AnnotationSpringRedisOperationSource.class);
			sourceDef.setSource(eleSource);
			sourceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			if (element.hasAttribute(SpringRedisConstants.ATTR_ANNOTATIONDRIVEN_REDISTEMPLATE)) {
				sourceDef.getPropertyValues().add("redisTemplate",
						element.getAttribute(SpringRedisConstants.ATTR_ANNOTATIONDRIVEN_REDISTEMPLATE));
			}
			String sourceName = parserContext.getReaderContext().registerWithGeneratedName(sourceDef);

			// Interceptor
			RootBeanDefinition interceptorDef = new RootBeanDefinition(SpringRedisInterceptor.class);
			interceptorDef.setSource(eleSource);
			interceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			interceptorDef.getPropertyValues().add("operationSource", new RuntimeBeanReference(sourceName));
			if (element.hasAttribute(SpringRedisConstants.ATTR_ANNOTATIONDRIVEN_EXCEPTIONHANDLER)) {
				sourceDef.getPropertyValues().add("redisTemplate",
						element.getAttribute(SpringRedisConstants.ATTR_ANNOTATIONDRIVEN_EXCEPTIONHANDLER));
			}
			String interceptorName = parserContext.getReaderContext().registerWithGeneratedName(interceptorDef);

			// Advisor
			RootBeanDefinition advisorDef = new RootBeanDefinition(BeanFactorySpringRedisOperationSourceAdvisor.class);
			advisorDef.setSource(eleSource);
			advisorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			advisorDef.getPropertyValues().add("operationSource", new RuntimeBeanReference(sourceName));
			advisorDef.getPropertyValues().add("adviceBeanName", interceptorName);
			if (element.hasAttribute(SpringRedisConstants.ATTR_ANNOTATIONDRIVEN_ORDER)) {
				advisorDef.getPropertyValues().add("order",
						element.getAttribute(SpringRedisConstants.ATTR_ANNOTATIONDRIVEN_ORDER));
			}
			parserContext.getRegistry().registerBeanDefinition(SpringRedisConstants.SPRINGREDIS_ADVISOR_BEAN_NAME,
					advisorDef);

			CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(),
					eleSource);
			compositeDef.addNestedComponent(new BeanComponentDefinition(sourceDef, sourceName));
			compositeDef.addNestedComponent(new BeanComponentDefinition(interceptorDef, interceptorName));
			compositeDef.addNestedComponent(new BeanComponentDefinition(advisorDef,
					SpringRedisConstants.SPRINGREDIS_ADVISOR_BEAN_NAME));
			parserContext.registerComponent(compositeDef);

			LOGGER.info("[SpringRedis][AnnotationDriven]-init-finish");
		}
		return null;
	}

}

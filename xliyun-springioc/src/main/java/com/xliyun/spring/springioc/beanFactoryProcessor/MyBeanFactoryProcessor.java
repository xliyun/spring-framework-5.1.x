package com.xliyun.spring.springioc.beanFactoryProcessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: xiaoliyu
 * @date: 2020-05-04 17:03
 */
//@Component//加上了注解，就不是自定义的BeanFactoryPostProcessor，需要交给spring管理
public class MyBeanFactoryProcessor implements BeanFactoryPostProcessor {
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanFactory.getBeanDefinition("indexDao");
		annotatedBeanDefinition.setScope("prototype");
	}
}

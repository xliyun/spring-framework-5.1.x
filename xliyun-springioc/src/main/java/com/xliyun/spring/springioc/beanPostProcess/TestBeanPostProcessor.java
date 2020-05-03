package com.xliyun.spring.springioc.beanPostProcess;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

/**
 * @description:
 * @author: xiaoliyu
 * @date: 2020-05-03 15:31
 */
@Component
public class TestBeanPostProcessor implements BeanPostProcessor, PriorityOrdered {
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if(beanName.equals("indexDao")){
			System.out.println("============postProcessBeforeInitialization==========");
		}
		/**
		 * AOP就是在这里返回代理对象，可以参考BeanPostProcessor的一个实现类：AbstractAutoPorxyCreator
		 */
		//Proxy.newProxyInstance()
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if(beanName.equals("indexDao")){
			System.out.println("============postProcessAfterInitialization==========");
		}
		return bean;
	}

	@Override
	public int getOrder() {
		return 101;
	}
}

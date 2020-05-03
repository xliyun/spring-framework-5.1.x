package com.xliyun.spring.springioc.beanPostProcess;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: xiaoliyu
 * @date: 2020-05-03 15:31
 */
@Component
public class TestBeanPostProcessor2 implements BeanPostProcessor, PriorityOrdered {
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if(beanName.equals("indexDao")){
			System.out.println("============postProcessBeforeInitialization2==========");
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
			System.out.println("============postProcessAfterInitialization2==========");
		}
		return bean;
	}

	@Override
	public int getOrder() {
		return 102;
	}
}

package com.xliyun.spring.springioc.dao;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

/**
 * @description:
 * @author: xiaoliyu
 * @date: 2020-05-02 11:50
 */
@Repository
@Scope("singleton")
@Description("dao")
public class IndexDao implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	public IndexDao(){
		System.out.println("构造方法");
	}

	@PostConstruct
	public void init(){
		System.out.println("init");
	}

	public void query(){
		System.out.println("测试");
		//如果我们的类是原型的，而我们的spring又是单例的，通过这种方式获取一个原型的indexDao的Bean
		//ApplicationContextAware 是(class ApplicationContextAwareProcessor implements BeanPostProcessor)里判断，提供applicationContext
		this.applicationContext.getBean("indexDao");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext=applicationContext;
	}
}

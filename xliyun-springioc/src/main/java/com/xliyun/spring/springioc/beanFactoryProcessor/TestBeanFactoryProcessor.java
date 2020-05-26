package com.xliyun.spring.springioc.beanFactoryProcessor;

import com.xliyun.spring.springioc.app.AppConfigNotHaveBeanFactoryProcessor;
import com.xliyun.spring.springioc.dao.IndexDao;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @description:
 * @author: xiaoliyu
 * @date: 2020-05-04 22:14
 */
public class TestBeanFactoryProcessor {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext annotationConfigApplicationContext =new AnnotationConfigApplicationContext();
		annotationConfigApplicationContext.register(AppConfigNotHaveBeanFactoryProcessor.class);

		annotationConfigApplicationContext.addBeanFactoryPostProcessor(new MyBeanFactoryProcessorByHand());
		annotationConfigApplicationContext.refresh();

		IndexDao indexDao = (IndexDao) annotationConfigApplicationContext.getBean("indexDao");
		IndexDao indexDao2 = (IndexDao) annotationConfigApplicationContext.getBean("indexDao");

		//因为indexDao已经被改成prototype原型模式，他俩的hashcode不一样了
		System.out.println("indexDao的hashCode: "+indexDao.hashCode());
		System.out.println("indexDao2的hashCode: "+indexDao2.hashCode());
	}
}

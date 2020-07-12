package com.xliyun.spring.springioc.beanFactoryProcessor;

import com.xliyun.spring.springioc.app.AppConfig;
import com.xliyun.spring.springioc.dao.IndexDao;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @description:
 * @author: xiaoliyu
 * @date: 2020-05-04 17:06
 */
public class Test2 {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext annotationConfigApplicationContext =new AnnotationConfigApplicationContext();
		annotationConfigApplicationContext.register(AppConfig.class);
		annotationConfigApplicationContext.refresh();

		IndexDao indexDao = (IndexDao) annotationConfigApplicationContext.getBean("indexDao");
		IndexDao indexDao2 = (IndexDao) annotationConfigApplicationContext.getBean("indexDao");

		//因为indexDao已经被改成prototype原型模式，他俩的hashcode不一样了
		System.out.println("indexDao的hashCode: "+indexDao.hashCode());
		System.out.println("indexDao2的hashCode: "+indexDao2.hashCode());
	}
}

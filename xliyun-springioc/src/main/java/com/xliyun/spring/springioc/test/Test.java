package com.xliyun.spring.springioc.test;

import com.xliyun.spring.springioc.app.AppConfig;
import com.xliyun.spring.springioc.dao.IndexDao;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @description:
 * @author: xiaoliyu
 * @date: 2020-05-02 11:53
 */
public class Test {
	public static void main(String[] args) {
		//把spring所有的前提环境准备
		//1、准备工厂 = DefaultListableBeanFacotry
		//实例化一个bdReader和一个Scanner
		AnnotationConfigApplicationContext annotationConfigApplicationContext =new AnnotationConfigApplicationContext();
		//把传递的class转成bd,最后put到DefaultListableBeanFacotry的map中去
		annotationConfigApplicationContext.register(AppConfig.class);

		//refresh()方法作用是初始化spring的环境
		annotationConfigApplicationContext.refresh();
		IndexDao bean = annotationConfigApplicationContext.getBean(IndexDao.class);
		bean.query();
	}
}

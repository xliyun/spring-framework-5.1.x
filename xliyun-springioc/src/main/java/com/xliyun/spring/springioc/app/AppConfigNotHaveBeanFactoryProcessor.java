package com.xliyun.spring.springioc.app;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * @description:
 * @author: xiaoliyu
 * @date: 2020-05-02 11:51
 */
@Configuration
@ComponentScan(value = "com.xliyun.spring.springioc"
,excludeFilters = {@ComponentScan.Filter(type = FilterType.REGEX,pattern = "com.xliyun.spring.springioc.beanFacotryProcessor.*")})

public class AppConfigNotHaveBeanFactoryProcessor {
}

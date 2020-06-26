package com.xliyun.spring.springioc.app;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: xiaoliyu
 * @date: 2020-05-02 11:51
 */
@Configuration
@ComponentScan(value = "com.xliyun.spring.springioc")
@Order(value = 1)
public class AppConfig {
}

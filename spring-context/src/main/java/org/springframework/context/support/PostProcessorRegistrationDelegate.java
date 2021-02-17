/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.Nullable;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {

	private PostProcessorRegistrationDelegate() {
	}


	//beanFactoryPostProcessors是程序员自定义的
	//所以是先执行程序员自定的，再去执行系统自带的
	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<>();

		//判断beanFactory是否实现了BeanDefinitionRegistry这个接口
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			//下面两个list
			// BeanDefinitionRegistryPostProcessor也是继承了BeanFactoryPostProcessor但是多了一个方法postProcessBeanDefinitionRegistry()
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>(); //放程序员手动annotationConfigApplicationContext.addBeanFactoryPostProcessor(new MyBeanFactoryProcessorByHand());的
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			//将传递进来的beanFactoryPostProcessors按照实现父类接口还是子类接口分别放到两个list当中，第一次执行不走这里！！（这里需要手动注册的，不加注解的）
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				//MyBeanFactoryProcessorByHand implements BeanFactoryPostProcessor 所以不走这个
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					//BeanDefinitionRegistryPostProcessor比父接口BeanFactoryPostProcessor多了一个postProcessBeanDefinitionRegistry()方法给执行掉
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				}
				//这里我们自己定义的MyBeanFactoryProcessorByHand也会走这个遍历
				else {
					//将MyBeanFactoryProcessorByHand 放到List<BeanFactoryPostProcessor>这个list当中
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.
			//这个currentRegistryProcessors是spring自己内部实现的继承自BeanDefinitionRegistryPostProcessor的接口
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			String[] postProcessorNames =
					//这个方法是根据提供的bd.class返回beanName
					//这里帮助后面返回ConfigurationClassPostProcessor（第一次执行是org.springframework.context.annotation.internalConfigurationAnnotationProcessor）
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			/**
			 * 这个地方可以得到一个BeanFactoryPostPreocessor,因为是spring默认在最开始的时候注册的
			 * 为什么要在最开始注册这个呢？
			 * 因为spring的工厂需要去解析去扫描等等功能
			 * 而这些功能都是需要在spring工厂初始化之前执行
			 * 要么在工厂最开始的时候、要么在工厂初始化之中，反正不能在之后
			 * 因为如果在之后就没有意义，因为那个时候已经需要使用工厂了
			 * 所以这里spring在一开始就注册了一个BeanFactoryPostProcessor,用来插手springfactory实例化过程
			 * 在这个地方断点就可以知道这个类叫做ConfigurationClassPostProcessor
			 * 他能插手spring工厂实例化过程
			 */
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					//从beanFactory当中把ConfigurationClassPostProcessor拿出来，
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			//排序不重要，这里第一次执行只会拿到org.springframework.context.annotation.ConfigurationClassPostProcessor
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			//合并list不重要，就是把spring默认的合并到自己注册的
			registryProcessors.addAll(currentRegistryProcessors);
			//循环调用BeanDefinitionRegistryPostProcessor的postProcessBeanDefinitionRegistry方法
			//！！！！最重要，因为这个方法就是调用执行所有的BeanDefinitionRegistryPostProcessor 执行的是ConfigurationClassPostProcessor扫描包的过程//BeanDefinitionRegistryPostProcessor比父接口BeanFactoryPostProcessor多了一个postProcessBeanDefinitionRegistry()方法给执行掉
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			//执行完了所有的BeanDefinitionRegistryPostProcessor
			//这个list只是一个临时变量，用完了，所以要清除
			currentRegistryProcessors.clear();

			//这段代码是判断新扫描出来的是不是还包含BeanDefinitionRegistryPostProcessor，就再处理一遍，跟上面的代码一样
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			//遍历这些BeanDefinitionRegistryPostProcessor，如果是上面那段没有处理过的，再处理
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				//将自己手动加的和spring初始化的PostProcessor合并
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			//执行BeanFacotryPostProcessor的回调，
			//前面执行的BeanFactoryPostProcessor的子类BeanDefinitionRegistryPostProcessor的回调
			//这次执行的是BeanFactoryPostProcessor
			//上面执行的invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry); currentRegistryProcessorss是BeanDefinitionRegistryPostProcessor

			/**
			 * 下面的代码是要执行继承了BeanFactoryPostProcessor的类，执行BeanFactoryPostProcessor的回调
			 *  前面invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry); 执行实现了BeanDefinitionRegistryPostProcessor接口的类，这些类，同时也是实现了BeanDefinitionRegistryPostProcessor父接口的方法，
			 *  在这里先执行实现了BeanDefinitionRegistryPostProcessor接口的类的postProcessBeanFactory方法，然后再去执行实现了父接口BeanFactoryPostProcessor的类的postProcessBeanFactory方法
			 *  比如我们这里暂时只有ConfigurationClassPostProcessor 就是实现了了BeanDefinitionRegistryPostProcessor接口
			 *  和父接口BeanFactoryPostProcessor的postProcessBeanFactory方法
			 */
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
			//执行自定义的BeanFactoryPostProcessor
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}

		else {
			// Invoke factory processors registered with the context instance.
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		//拿出所有的BeanFactoryPostProcessor，
		//只有ConfigurationClassPostProcessor
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);
		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			//如果ppName包含在上面处理过的set当中，就什么都不做，如果不包含，就是扫描过程中新增加的
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}

	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {
		/**
		 * 这里初始化的时候会拿到三个后置处理器，其中一个就是处理ApplicationContextAware
		 * 是在前面prepareBeanFactory(beanFactory);初始化spring的时候添加了beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this)); beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));
		 * 还有一个是invokeBeanFactoryPostProcessors(beanFactory);cglib增强的时候增加的ImportAwareBeanPostProcessor
		 *
		 */
        //从beanDefinitionMap中得到所有的BeanPostProcessors，然后把它们注册到BeanPostProcessors里面
		//我们自己添加的后置处理器也会在这里被拿出来注册（我们自己添加可以通过加@Import注解）
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		//将BeanPostProcessor注册到beanFactory的beanPostProcessor当中，待后面执行处理
		//调试到这里是有7个的
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {

		//循环调用系统自带的processor BeanDefinitionRegistryPostProcessor的扩展方法
		//这里调试的时候传递的是 org.springframework.context.annotation.ConfigurationClassPostProcessor
		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanDefinitionRegistry(registry);
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 * 当spring的配置中后置处理器还没有被注册就已经开始了bean初始化==>检查bean没有经过后置处理器的处理就进行了实例化
	 * 便会打印出BeanPostProcessorChecker中设定的信息
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			//检查通过的后置处理器的长度
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}

}

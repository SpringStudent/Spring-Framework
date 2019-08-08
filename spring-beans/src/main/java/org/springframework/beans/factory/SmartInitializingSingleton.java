/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.beans.factory;

/**
 * 在{@link BeanFactory}引导期间，在单例预实例化阶段结束时触发了回调接口。
 * 此接口可以由单例bean实现，以便在常规单例实例化算法之后执行一些初始化,
 * 避免意外早期初始化的副作用（例如来自{@link ListableBeanFactory＃getBeansOfType}调用）.
 * 从某个意义上来说他是在bean本地构建阶段结束触发的{@link ListableBeanFactory#getBeansOfType}
 * 替代
 *
 * <p>此回调变体有点类似于{@link org.springframework.context.event.ContextRefreshedEvent}，
 * 但不需要{@link org.springframework.context.ApplicationListener的实现
 * 无需跨上下文层次结构等过滤上下文引用.
 * 它还意味着对{@code beans}包的依赖性更小，并且受到独立的{@link ListableBeanFactory}实现的尊重，
 * 而不仅仅是在{@link org.springframework.context.ApplicationContext}环境中。
 *
 * <p><b>NOTE:</b> 注意：</ b>如果您打算启动/管理异步任务，
 * 最好实现{@link org.springframework.context.Lifecycle}，它为运行时管理提供了更丰富的模型，并允许分阶段启动/关闭
 *
 * @author Juergen Hoeller
 * @since 4.1
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#preInstantiateSingletons()
 */
public interface SmartInitializingSingleton {

	/**
	 * 在单例预实例化阶段结束时调用，
	 * 保证所有常规单例的bean都已经已创建
	 * 在引导期间的{@link ListableBeanFactory＃getBeansOfType}调用
	 * 不会触发意外的副作用
	 * <p><b>NOTE:</b> This callback won't be triggered for singleton beans
	 * lazily initialized on demand after {@link BeanFactory} bootstrap,
	 * and not for any other bean scope either. Carefully use it for beans
	 * with the intended bootstrap semantics only.
	 * 这个回调对于{@link BeanFactory}的引导命令的延迟加载的单例beans不会触发
	 * 而且也不适用于任何其他bean范围。 小心地将它用于beans
	 *   仅具有预期的引导语义
	 */
	void afterSingletonsInstantiated();

}

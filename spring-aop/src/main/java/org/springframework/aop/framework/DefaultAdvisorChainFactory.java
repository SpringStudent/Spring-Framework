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

package org.springframework.aop.framework;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.Advisor;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.support.MethodMatchers;

/**
 * A simple but definitive way of working out an advice chain for a Method,
 * given an {@link Advised} object. Always rebuilds each advice chain;
 * caching can be provided by subclasses.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 2.0.3
 */
@SuppressWarnings("serial")
public class DefaultAdvisorChainFactory implements AdvisorChainFactory, Serializable {

	@Override
	public List<Object> getInterceptorsAndDynamicInterceptionAdvice(
			Advised config, Method method, Class<?> targetClass) {

		// This is somewhat tricky... We have to process introductions first,
		// but we need to preserve order in the ultimate list.
		// 用于保存拦截器的集合
		List<Object> interceptorList = new ArrayList<Object>(config.getAdvisors().length);
		// 方法对应的targetClass不是代理类的
		Class<?> actualClass = (targetClass != null ? targetClass : method.getDeclaringClass());
		//是否有intruction类型的增强
		boolean hasIntroductions = hasMatchingIntroductions(config, actualClass);
		//获取适配器DefaultAdvisorAdapterRegistry，用于将advisor封装为interceptor
		AdvisorAdapterRegistry registry = GlobalAdvisorAdapterRegistry.getInstance();
		//遍历所有增强
		for (Advisor advisor : config.getAdvisors()) {
			//如果是PointcutAdvisor
			if (advisor instanceof PointcutAdvisor) {
				// Add it conditionally.
				PointcutAdvisor pointcutAdvisor = (PointcutAdvisor) advisor;
				// config.isPreFiltered() -> 返回是否对该代理配置进行了预筛选，以便仅对其进行筛选包含适用的增强(匹配此代理的目标类)。
				// pointcutAdvisor.getPointcut().getClassFilter().matches(actualClass) -> 当前切点匹配的类是否匹配actualClass
				// 以上两个条件是在类一级别上做出判断,如果符合,则接下来对方法级别的再做匹配判断
				if (config.isPreFiltered() || pointcutAdvisor.getPointcut().getClassFilter().matches(actualClass)) {
					// 获取当前切点匹配的方法
					MethodMatcher mm = pointcutAdvisor.getPointcut().getMethodMatcher();
					//advisor的method也匹配上了当前方法
					if (MethodMatchers.matches(mm, method, actualClass, hasIntroductions)) {
						//转换为interceptors
						MethodInterceptor[] interceptors = registry.getInterceptors(advisor);
						//如果是运行时的说明还需要对方法进行boolean matches(Method method, Class<?> targetClass, Object... args);判断
						//这个方法判断下能否匹配
						if (mm.isRuntime()) {
							// Creating a new object instance in the getInterceptors() method
							// isn't a problem as we normally cache created chains.
							for (MethodInterceptor interceptor : interceptors) {
								interceptorList.add(new InterceptorAndDynamicMethodMatcher(interceptor, mm));
							}
						}
						else {
							//直接添加所有拦截器
							interceptorList.addAll(Arrays.asList(interceptors));
						}
					}
				}
			}
			//如果是属于introduction类型的Advisor
			else if (advisor instanceof IntroductionAdvisor) {
				IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
				if (config.isPreFiltered() || ia.getClassFilter().matches(actualClass)) {
					Interceptor[] interceptors = registry.getInterceptors(advisor);
					interceptorList.addAll(Arrays.asList(interceptors));
				}
			}
			else {
				//其他类型
				Interceptor[] interceptors = registry.getInterceptors(advisor);
				interceptorList.addAll(Arrays.asList(interceptors));
			}
		}

		return interceptorList;
	}

	/**
	 * Determine whether the Advisors contain matching introductions.
	 *
	 * 判断方法有没有Intruction类型的增强
	 */
	private static boolean hasMatchingIntroductions(Advised config, Class<?> actualClass) {
		for (Advisor advisor : config.getAdvisors()) {
			if (advisor instanceof IntroductionAdvisor) {
				IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
				if (ia.getClassFilter().matches(actualClass)) {
					return true;
				}
			}
		}
		return false;
	}

}

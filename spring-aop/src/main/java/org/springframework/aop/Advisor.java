/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.aop;

import org.aopalliance.aop.Advice;

/**
 * 持有AOP advice(增强,在连接点要执行的动作可以理解为补充逻辑)以及确定advice
 * 适用性的过滤器（例如切入点）的基本接口, <i>此接口不供Spring用户使用，
 * 但允许支持不同类型的advice
 *
 * springAOP是符合AOP Alliance拦截api规范的，并且通过方法拦截，
 * 这个接口支持不同使用interception类型的advice(增强),比如<b>before</b> and <b>after</b>
 *
 * @author Rod Johnson
 */
public interface Advisor {

	/**
	 * 返回advice，例如BeforeAdvice、ThrowsAdvice、MethodInterceptor
	 * 、AfterReturningAdvice
	 * @return the advice that should apply if the pointcut matches
	 * @see org.aopalliance.intercept.MethodInterceptor
	 * @see BeforeAdvice
	 * @see ThrowsAdvice
	 * @see AfterReturningAdvice
	 */
	Advice getAdvice();

	/**
	 * 返回当前的advice是否与一个特定的实例相关,或者与从同一个Spring bean工
	 * 厂获得的增强的类的所有实例共享
	 * <p>注意框架当前没有使用此方法。<p/>
	 * Return whether this advice is associated with a particular instance
	 * (for example, creating a mixin) or shared with all instances of
	 * the advised class obtained from the same Spring bean factory.
	 * <p><b>Note that this method is not currently used by the framework.</b>
	 * Typical Advisor implementations always return {@code true}.
	 * Use singleton/prototype bean definitions or appropriate programmatic
	 * proxy creation to ensure that Advisors have the correct lifecycle model.
	 * @return whether this advice is associated with a particular target instance
	 */
	boolean isPerInstance();

}

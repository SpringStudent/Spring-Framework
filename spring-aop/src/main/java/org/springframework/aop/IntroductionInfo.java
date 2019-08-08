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

/**
 * Interface supplying the information necessary to describe an introduction.
 *
 * 提供要描述一个introduction必须信息的接口
 *
 * {@link IntroductionAdvisor IntroductionAdvisors}实现了此接口,如果一个
 * {@link org.aopalliance.aop.Advice}接口实现了这个接口，他可以用作{@link IntroductionAdvisor}
 * 的一个introduction,在此种情形下，advice是self-describing的,不仅仅提供必要的
 * 行为，还描述introduction的接口
 *
 * @author Rod Johnson
 * @since 1.1.1
 */
public interface IntroductionInfo {

	/**
	 * Return the additional interfaces introduced by this Advisor or Advice.
	 * 返回此Advisor或Advice引入的其他接口
	 * @return the introduced interfaces
	 */
	Class<?>[] getInterfaces();

}

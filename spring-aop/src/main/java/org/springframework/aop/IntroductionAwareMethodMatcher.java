/*
 * Copyright 2002-2013 the original author or authors.
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

import java.lang.reflect.Method;

/**
 *
 * 一种特殊类型的{@link MethodMatcher}，在匹配方法时会考虑引入
 * 例如，如果没有introductions在目标类上，方法匹配器可能能够更有效地优化匹配
 * @author Adrian Colyer
 * @since 2.0
 */
public interface IntroductionAwareMethodMatcher extends MethodMatcher {

	/**
	 *
	 * 执行静态检查给定方法是否匹配。 如果调用者支持扩展的IntroductionAwareMethodMatcher接口，
	 * 则可以调用此方法而不是2个参数的 {@link #matches（java.lang.reflect.Method，Class）}方法
	 * @param method the candidate method
	 * @param targetClass the target class (may be {@code null}, in which case
	 * the candidate class must be taken to be the method's declaring class)
	 * @param hasIntroductions {@code true} if the object on whose behalf we are
	 * asking is the subject on one or more introductions; {@code false} otherwise
	 * @return whether or not this method matches statically
	 */
	boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions);

}

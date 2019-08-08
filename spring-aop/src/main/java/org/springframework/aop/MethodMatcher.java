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

package org.springframework.aop;

import java.lang.reflect.Method;

/**
 *
 * {@link Pointcut}的一部分：检查目标方法是否符合advice的条件
 *
 * MethodMatcher可能被 <b>statically</b> or at <b>runtime</b> 的evaluated
 * 静态匹配涉及方法和（可能）方法属性，动态匹配还使特定调用的参数可用，
 * 以及运行应用于连接点的先前advice的任何效果
 *
 * 如果实现从其{@link #isRuntime（）}方法返回{@code false}，
 * 则可以静态执行评估，并且对于此方法的所有调用，结果都是相同的，无论其参数如何
 * 这意味着如果{@link #isRuntime（）}方法返回{@code false}，
 * 那么3个参数 {@link #matches（java.lang.reflect.Method，Class，Object []）}方法将永远不会调用
 *
 *
 * 如果实现从其2-arg {@link #matches（java.lang.reflect.Method，Class）}方法返回{@code true}
 * 并且其{@link #isRuntime（）}方法返回{@code true}， 3-arg
 * {@link #matches（java.lang.reflect.Method，Class，Object []）}
 * 方法将在每次可能执行相关advice</ i>之前立即调用<i>，以决定是否 advice应该运行
 * 所有先前的增强例如拦截器链中的早期拦截器将会运行，
 * 因此在评估时可以获得它们在参数或ThreadLocal状态下产生的任何状态更改
 * @author Rod Johnson
 * @since 11.11.2003
 * @see Pointcut
 * @see ClassFilter
 */
public interface MethodMatcher {

	/**
	 * 执行静态检查给定方法是否匹配
	 * <p>如果返回{@code false}或者{@link #isRuntime（）}方法返回{@code false}，
	 * 则不进行运行时检查（即没有{@link #matches（java.lang.reflect.Method，Class，Object） []）}调用）
	 * 将被使用
	 * @param method the candidate method
	 * @param targetClass the target class (may be {@code null}, in which case
	 * the candidate class must be taken to be the method's declaring class)
	 * @return whether or not this method matches statically
	 */
	boolean matches(Method method, Class<?> targetClass);

	/**
	 * 这个MethodMatcher是动态的，也就是说，必须在运行时对{@link #matches（java.lang.reflect.Method，Class，Object []）}
	 * 方法进行最终调用，即使2-arg matches方法返回{ @code true}
	 * <p>当创建AOP代理时调用,不必在每个method调用之前都调用
	 * @return whether or not a runtime match via the 3-arg
	 * {@link #matches(java.lang.reflect.Method, Class, Object[])} method
	 * is required if static matching passed
	 */
	boolean isRuntime();

	/**
	 * Check whether there a runtime (dynamic) match for this method,
	 * which must have matched statically.
	 * <p>This method is invoked only if the 2-arg matches method returns
	 * {@code true} for the given method and target class, and if the
	 * {@link #isRuntime()} method returns {@code true}. Invoked
	 * immediately before potential running of the advice, after any
	 * advice earlier in the advice chain has run.
	 * @param method the candidate method
	 * @param targetClass the target class (may be {@code null}, in which case
	 * the candidate class must be taken to be the method's declaring class)
	 * @param args arguments to the method
	 * @return whether there's a runtime match
	 * @see MethodMatcher#matches(Method, Class)
	 */
	boolean matches(Method method, Class<?> targetClass, Object... args);


	/**
	 * Canonical instance that matches all methods.
	 */
	MethodMatcher TRUE = TrueMethodMatcher.INSTANCE;

}

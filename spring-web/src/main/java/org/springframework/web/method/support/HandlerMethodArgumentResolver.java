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

package org.springframework.web.method.support;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;

/**
 *
 * 用于在给定请求的上下文中将方法参数解析为参数值的策略接口。
 * @author Arjen Poutsma
 * @since 3.1
 * @see HandlerMethodReturnValueHandler
 */
public interface HandlerMethodArgumentResolver {

	/**
	 * 此解析程序是否支持给定的{@linkplain MethodParameter方法参数}。
	 * @param parameter the method parameter to check
	 * @return {@code true} if this resolver supports the supplied parameter;
	 * {@code false} otherwise
	 */
	boolean supportsParameter(MethodParameter parameter);

	/**
	 * 将给定请求的方法参数解析为 参数值
	 * {@link ModelAndViewContainer}提供对请求模型(model)的访问。
	 * {@link WebDataBinderFactory}提供了当需要数据binding和类型转换
	 * 时创建{@link WebDataBinder}的方式
	 * @param parameter the method parameter to resolve. This parameter must
	 * have previously been passed to {@link #supportsParameter} which must
	 * have returned {@code true}.
	 * @param mavContainer the ModelAndViewContainer for the current request
	 * @param webRequest the current request
	 * @param binderFactory a factory for creating {@link WebDataBinder} instances
	 * @return the resolved argument value, or {@code null}
	 * @throws Exception in case of errors with the preparation of argument values
	 */
	Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception;

}

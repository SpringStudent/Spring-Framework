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
import org.springframework.web.context.request.NativeWebRequest;

/**
 *
 * 处理由处理程序的调用返回值的策略接口
 *
 * @author Arjen Poutsma
 * @since 3.1
 * @see HandlerMethodArgumentResolver
 */
public interface HandlerMethodReturnValueHandler {

	/**
	 * 给定的{@linkplain MethodParameter return tpye方法}是否被当前处理器支持
	 * {@linkplain MethodParameter 方法返回类型}是否支持该handler
	 * @param returnType the method return type to check
	 * @return {@code true} if this handler supports the supplied return type;
	 * {@code false} otherwise
	 */
	boolean supportsReturnType(MethodParameter returnType);

	/**
	 * 通过添加model的属性和设置视图处理返回值或者设置{@link ModelAndViewContainer#setRequestHandled}
	 * 标识为true去表示相应已被直接处理
	 * @param returnValue the value returned from the handler method
	 * @param returnType the type of the return value. This type must have
	 * previously been passed to {@link #supportsReturnType} which must
	 * have returned {@code true}.
	 * @param mavContainer the ModelAndViewContainer for the current request
	 * @param webRequest the current request
	 * @throws Exception if the return value handling results in an error
	 */
	void handleReturnValue(Object returnValue, MethodParameter returnType,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception;

}

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

package org.springframework.web.servlet.handler;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;

/**
 * 拦截器将已配置的{@link ConversionService}置于请求范围内，以便在请求处理期间可用。
 * 请求属性名称是“org.springframework.core.convert.ConversionService”，其值为
 * {@code ConversionService.class.getName（）}。
 *
 * <p>主要用于JSP标记，例如spring：eval标记。
 *
 * @author Keith Donald
 * @since 3.0.1
 */
public class ConversionServiceExposingInterceptor extends HandlerInterceptorAdapter {

	private final ConversionService conversionService;


	/**
	 * Creates a new {@link ConversionServiceExposingInterceptor}.
	 * @param conversionService the conversion service to export to request scope when this interceptor is invoked
	 */
	public ConversionServiceExposingInterceptor(ConversionService conversionService) {
		Assert.notNull(conversionService, "The ConversionService may not be null");
		this.conversionService = conversionService;
	}


	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws ServletException, IOException {

		request.setAttribute(ConversionService.class.getName(), this.conversionService);
		return true;
	}

}

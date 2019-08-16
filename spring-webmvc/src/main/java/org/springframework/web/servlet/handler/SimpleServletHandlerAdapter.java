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

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

/**
 * 使DispatcherServlet适配处理Servlet请求。
 * 调用Servlet的{@code service}方法来处理请求。
 *
 * <p>Last-modified没有被明确的支持
 * 这通常由Servlet实现本身处理（通常从HttpServlet基类派生）。
 *
 * <p>默认情况下不会激活此适配器; 它需要定义为
 *   DispatcherServlet上下文中的bean。
 * 它会自动的应用去处理那些实现了Servlet接口的Bean.
 *
 * <p>定义为bean的servlet实例将不会执行initialization和
 * destroy方法回调。
 * 除非在DispatcherServlet上下文中定义了一个特殊的后处理器，如SimpleServletPostProcessor。
 *
 * <p><b>或者，考虑使用Spring包装Servlet为一个 ServletWrappingController。</ b>
 * 这特别适用于现有的Servlet类，允许指定Servlet初始化参数等。
 *
 * @author Juergen Hoeller
 * @since 1.1.5
 * @see javax.servlet.Servlet
 * @see javax.servlet.http.HttpServlet
 * @see SimpleServletPostProcessor
 * @see org.springframework.web.servlet.mvc.ServletWrappingController
 */
public class SimpleServletHandlerAdapter implements HandlerAdapter {

	@Override
	public boolean supports(Object handler) {
		return (handler instanceof Servlet);
	}

	@Override
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		((Servlet) handler).service(request, response);
		return null;
	}

	@Override
	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1;
	}

}

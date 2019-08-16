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

package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MVC框架SPI，允许核心MVC工作流的参数化。
 *
 * 每个类型的处理程序必须实现他去处理一个请求.
 * 此接口用于允许{@link DispatcherServlet}无限扩展。
 * {@code DispatcherServlet}通过这个接口访问所有已经装配的handlers,
 * 意味着它不包含特定于任何处理程序类型的代码。
 *
 * <p>请注意，处理程序可以是{@code Object}类型。
 * 这是为了使其他框架的处理程序能够与此框架集成，
 * 而无需自定义编码，以及允许不遵循任何特定Java接口的注释驱动的处理程序对象。
 *
 * <p>此接口不适用于应用程序开发人员。它对那些想要开发web工作流处理程序的人很有用
 *
 * <p>Note: {@code HandlerAdapter} implementors may implement the {@link
 * org.springframework.core.Ordered} interface to be able to specify a sorting
 * order (and thus a priority) for getting applied by the {@code DispatcherServlet}.
 * Non-Ordered instances get treated as lowest priority.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter
 * @see org.springframework.web.servlet.handler.SimpleServletHandlerAdapter
 */
public interface HandlerAdapter {

	/**
	 * 给定一个处理程序实例，返回此{@code HandlerAdapter}是否可以支持它。
	 * HandlerAdapters根据handler类型做出决定。
	 * HandlerAdapters通常每个只支持一种处理程序类型。
	 * <p>A typical implementation:
	 * <p>{@code
	 * return (handler instanceof MyHandler);
	 * }
	 * @param handler handler object to check
	 * @return whether or not this object can use the given handler
	 */
	boolean supports(Object handler);

	/**
	 * 使用给定的处理程序来处理此请求。
	 * 所需要的workflow可能有很大差异。
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler handler to use. This object must have previously been passed
	 * to the {@code supports} method of this interface, which must have
	 * returned {@code true}.
	 * @throws Exception in case of errors
	 * @return ModelAndView object with the name of the view and the required
	 * model data, or {@code null} if the request has been handled directly
	 */
	ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

	/**
	 *
	 * 同HttpServlet{@code getLastModified}一样的方法
	 * 如果处理程序类中没有支持，则可以简单地返回-1。
	 * @param request current HTTP request
	 * @param handler handler to use
	 * @return the lastModified value for the given handler
	 * @see javax.servlet.http.HttpServlet#getLastModified
	 * @see org.springframework.web.servlet.mvc.LastModified#getLastModified
	 */
	long getLastModified(HttpServletRequest request, Object handler);

}

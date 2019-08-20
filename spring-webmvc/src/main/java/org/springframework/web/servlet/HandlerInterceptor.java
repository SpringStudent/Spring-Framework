/*
 * Copyright 2002-2016 the original author or authors.
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

import org.springframework.web.method.HandlerMethod;

/**
 * 允许自定义执行链的工作流接口.
 * 应用程序可以为某些处理程序组注册任意数量的现有或自定义拦截器，以添加常见的预处理行为，而无需修改每个处理程序实现。
 *
 * <p>HandlerInterceptor会在HandlerAdapter触发处理之前适当的被调用.
 * 该机制可用于大范围的预处理方面，例如， 用于授权检查，或常见的处理程序行为，如区域设置或主题更改。
 * 其主要目的是允许分解重复的处理程序代码。
 *
 *
 * <p>In an asynchronous processing scenario, the handler may be executed in a
 * separate thread while the main thread exits without rendering or invoking the
 * {@code postHandle} and {@code afterCompletion} callbacks. When concurrent
 * handler execution completes, the request is dispatched back in order to
 * proceed with rendering the model and all methods of this contract are invoked
 * again. For further options and details see
 * {@code org.springframework.web.servlet.AsyncHandlerInterceptor}
 *
 * 在异步处理场景中,当主线程退出而不调用{@code postHandle}和{@code afterCompletion}回调时，处理程序可以在单独的线程中执行。
 * 并发处理程序执行完成后，将调度该请求以继续呈现模型，并再次调用此合同的所有方法。
 *
 * <p>Typically an interceptor chain is defined per HandlerMapping bean,
 * sharing its granularity. To be able to apply a certain interceptor chain
 * to a group of handlers, one needs to map the desired handlers via one
 * HandlerMapping bean. The interceptors themselves are defined as beans
 * in the application context, referenced by the mapping bean definition
 * via its "interceptors" property (in XML: a &lt;list&gt; of &lt;ref&gt;).
 * 通常，每个HandlerMapping bean定义一个拦截器链.
 * 为了能够将某个拦截器链应用于一组handlers，需要通过一个HandlerMapping bean映射所需的处理程序。
 * 拦截器本身在应用程序上下文中定义为bean，由映射bean定义通过其“拦截器”属性引用（在XML中：＆lt; list＆gt; of＆lt; ref＆gt;）。
 *
 * <p>HandlerInterceptor基本上类似于Servlet过滤器，但是在
 * 与后者相反，它只允许自定义预处理，禁止执行处理程序本身，以及自定义后处理。
 * 过滤器功能更强大，例如，它们允许交换传递链中的请求和响应对象。 请注意，过滤器在web.xml中配置
 * HandlerInterceptor配置在上下文程序中。
 *
 * <p>基本准则是,细粒度的handler相关的预处理任务的候选是最好是HandlerInterceptor的实现,
 * 尤其是公共的handler处理代码和授权检查.
 * 另一方面，过滤器非常适合请求内容和视图内容处理，如多部分表单和GZIP压缩。
 * 这通常表示何时需要将过滤器映射到某些内容类型（例如图像）或所有请求。
 * @author Juergen Hoeller
 * @since 20.06.2003
 * @see HandlerExecutionChain#getInterceptors
 * @see org.springframework.web.servlet.handler.HandlerInterceptorAdapter
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping#setInterceptors
 * @see org.springframework.web.servlet.handler.UserRoleAuthorizationInterceptor
 * @see org.springframework.web.servlet.i18n.LocaleChangeInterceptor
 * @see org.springframework.web.servlet.theme.ThemeChangeInterceptor
 * @see javax.servlet.Filter
 */
public interface HandlerInterceptor {

	/**
	 * 拦截处理程序的执行。 在HandlerMapping确定适当的处理程序对象之后调用，但在HandlerAdapter调用处理程序之前。
	 * <p>DispatcherServlet处理执行链中的处理程序，该处理程序由任意数量的拦截器组成，最后处理程序本身。
	 * 使用此方法，每个拦截器都可以决定中止执行链，通常发送HTTP错误或编写自定义响应。
	 * <p><strong>注意：</ strong>特殊注意事项适用于异步请求处理。 有关详细信息，请参阅
	 * {@link org.springframework.web.servlet.AsyncHandlerInterceptor}.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler chosen handler to execute, for type and/or instance evaluation
	 * @return {@code true} if the execution chain should proceed with the
	 * next interceptor or the handler itself. Else, DispatcherServlet assumes
	 * that this interceptor has already dealt with the response itself.
	 * @throws Exception in case of errors
	 */
	boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception;

	/**
	 * 拦截处理程序的执行。 在HandlerAdapter实际调用处理程序之后调用，但在DispatcherServlet呈现视图之前调用。
	 * 可以通过给定的ModelAndView将其他模型对象暴露给视图。
	 * <p>DispatcherServlet处理执行链中的处理程序，该处理程序由任意数量的拦截器组成，最后处理程序本身。
	 * 使用此方法，每个拦截器都可以对执行进行后处理，以执行链的逆序应用。
	 * <p><strong>注意：</ strong>特殊注意事项适用于异步请求处理。 有关详细信息，请参见链接。
	 * {@link org.springframework.web.servlet.AsyncHandlerInterceptor}.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler handler (or {@link HandlerMethod}) that started asynchronous
	 * execution, for type and/or instance examination
	 * @param modelAndView the {@code ModelAndView} that the handler returned
	 * (can also be {@code null})
	 * @throws Exception in case of errors
	 */
	void postHandle(
			HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
			throws Exception;

	/**
	 * 完成请求处理后回调，即渲染视图后回调。 将调用处理程序执行的任何结果，从而允许适当的资源清理。
	 * <p>注意：只有在这个拦截器{@code preHandle}时才会被调用
	 *   方法已成功完成并返回{@code true}！
	 * <p>与{@code postHandle}方法一样，该方法将以相反的顺序在链中的每个拦截器上调用，因此第一个拦截器将是最后一个被调用的拦截器。
	 * <p><strong>Note:</strong> special considerations apply for asynchronous
	 * request processing. For more details see
	 * {@link org.springframework.web.servlet.AsyncHandlerInterceptor}.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler handler (or {@link HandlerMethod}) that started asynchronous
	 * execution, for type and/or instance examination
	 * @param ex exception thrown on handler execution, if any
	 * @throws Exception in case of errors
	 */
	void afterCompletion(
			HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception;

}

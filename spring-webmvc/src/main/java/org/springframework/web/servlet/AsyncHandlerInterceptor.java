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
 * 使用在异步请求处理开始后调用的回调方法扩展{@code HandlerInterceptor}。
 *
 * <p>当处理程序启动异步请求时，{@link DispatcherServlet}退出时不会像通常对同步请求
 * 那样调用{@code postHandle}和{@code afterCompletion}，因为请求处理的结果（
 * 例如ModelAndView）可能不会 准备就绪，将从另一个线程同时生成。 在这种情况下，
 * 调用{@link #afterConcurrentHandlingStarted}，
 * 允许实现在将线程释放到Servlet容器之前执行诸如清理线程绑定属性之类的任务。
 *
 * <p>异步处理完成后，将请求分派给容器以进行进一步处理。
 * 在此阶段，{@code DispatcherServlet}调用{@code preHandle}，{@ code postHandle}和{@code afterCompletion}。
 * 为了区分异步处理完成后的初始请求和后续调度，拦截器可以检查{@link javax.servlet.ServletRequest}的
 * {@code javax.servlet.DispatcherType}是{@code"REQUEST"}还是{@code"ASYNC"}。
 *
 * <p>请注意，当异步请求超时或因网络错误而完成时，{@code HandlerInterceptor}实现可能需要执行。
 * 对于这种情况，Servlet容器不会调度，因此不会调用{@code postHandle}和{@code afterCompletion}方法。
 * 相反，拦截器可以通过{@link org.springframework.web.context.request.async.WebAsyncManager
 * 上的{@code registerCallbackInterceptor}和{@code registerDeferredResultInterceptor}方法注册以跟踪异步请求。
 * WebAsyncManager}。 这可以在每次请求时主动完成{@code preHandle}无论是否启动异步请求处理。
 *
 * @author Rossen Stoyanchev
 * @since 3.2
 * @see org.springframework.web.context.request.async.WebAsyncManager
 * @see org.springframework.web.context.request.async.CallableProcessingInterceptor
 * @see org.springframework.web.context.request.async.DeferredResultProcessingInterceptor
 */
public interface AsyncHandlerInterceptor extends HandlerInterceptor {

	/**
	 * 当一个处理程序并发执行时，调用而不是{@code postHandle}和{@code afterCompletion}。
	 * <p>实现可以使用提供的请求和响应，但应避免以与处理程序的并发执行冲突的方式修改它们。 此方法的典型用法是清理线程局部变量。
	 * @param request the current request
	 * @param response the current response
	 * @param handler the handler (or {@link HandlerMethod}) that started async
	 * execution, for type and/or instance examination
	 * @throws Exception in case of errors
	 */
	void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception;

}

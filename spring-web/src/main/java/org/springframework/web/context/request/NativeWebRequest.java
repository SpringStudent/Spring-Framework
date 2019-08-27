/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.web.context.request;

/**
 * {@link WebRequest}接口的扩展，以通用方式暴露了
 * 本地请求和响应对象。
 *
 * <p>主要用于框架内部使用，
 *   特别是对于通用参数解析代码。
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 */
public interface NativeWebRequest extends WebRequest {

	/**
	 * Return the underlying native request object.
	 * @see javax.servlet.http.HttpServletRequest
	 * @see javax.portlet.ActionRequest
	 * @see javax.portlet.RenderRequest
	 */
	Object getNativeRequest();

	/**
	 * Return the underlying native response object, if any.
	 * @see javax.servlet.http.HttpServletResponse
	 * @see javax.portlet.ActionResponse
	 * @see javax.portlet.RenderResponse
	 */
	Object getNativeResponse();

	/**
	 * Return the underlying native request object, if available.
	 * @param requiredType the desired type of request object
	 * @return the matching request object, or {@code null} if none
	 * of that type is available
	 * @see javax.servlet.http.HttpServletRequest
	 * @see javax.portlet.ActionRequest
	 * @see javax.portlet.RenderRequest
	 */
	<T> T getNativeRequest(Class<T> requiredType);

	/**
	 * Return the underlying native response object, if available.
	 * @param requiredType the desired type of response object
	 * @return the matching response object, or {@code null} if none
	 * of that type is available
	 * @see javax.servlet.http.HttpServletResponse
	 * @see javax.portlet.ActionResponse
	 * @see javax.portlet.RenderResponse
	 */
	<T> T getNativeResponse(Class<T> requiredType);

}

/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.web.multipart;

import javax.servlet.http.HttpServletRequest;

/**
 * 一种用于多部分文件上传解析的策略接口
 * 使用<a href="https://www.ietf.org/rfc/rfc1867.txt"> RFC 1867 </a>。
 * 实现通常可以在应用程序上下文中使用，也可以单独使用。
 *
 * <p>从Spring 3.1开始，Spring中包含两个具体实现：
 * <ul>
 * <li>用于Apache Commons FileUpload{@link org.springframework.web.multipart.commons.CommonsMultipartResolver}
 * <li>用于Servlet 3.0+ Part API{@link org.springframework.web.multipart.support.StandardServletMultipartResolver}
 * </ul>
 *
 * <p>Spring的{@link org.springframework.web.servlet.DispatcherServlet DispatcherServlets}
 * 没有默认的解析器，因为应用程序可能会选择自己解析其多部分请求。要定义实现，
 * 请在{@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet的}
 * 应用程序上下文中创建一个id为“multipartResolver”的bean。
 * 这样的解析器将应用于所有 通过{@link org.springframework.web.servlet.DispatcherServlet}处理的请求
 *  
 *
 * <p>如果{@link org.springframework.web.servlet.DispatcherServlet}检测到多部分请求，
 * 它将通过配置的{@link MultipartResolver}解析它并传递一个包装的{@link javax.servlet.http.HttpServletRequest}。
 * 控制器可以将他们给定的请求向下造型为{@link MultipartHttpServletRequest},
 * 该类型允许访问任何{@link MultipartFile MultipartFiles}。
 * 请注意，仅在实际的多部分请求的情况下才支持此强制转换。
 *
 * <pre class="code">
 * public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
 *   MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
 *   MultipartFile multipartFile = multipartRequest.getFile("image");
 *   ...
 * }</pre>
 *
 * 除了直接访问，请求或者表单controller可以注册一个
 * {@link org.springframework.web.multipart.support.ByteArrayMultipartFileEditor}
 *  * or {@link org.springframework.web.multipart.support.StringMultipartFileEditor}
 * 与他们的数据绑定.自动将多部分内容应用于表单bean属性。
 *
 * <p>作为通过{@link org.springframework.web.servlet.DispatcherServlet}使用{@link MultipartResolver}的替代方法，
 * 可以在{@code web.xml}中注册。 {@link org.springframework.web.multipart.support.MultipartFilter}
 * 它将委托给根应用程序上下文中的相应{@link MultipartResolver} bean。 这主要适用于不使用Spring自己的Web MVC框架的应用程序。
 *
 * <p>注意：几乎不需要从应用程序代码访问{@link MultipartResolver}本身。
 * 它将在幕后完成其工作，使控制器可以使用{@link MultipartHttpServletRequest MultipartHttpServletRequests}。
 *
 * @author Juergen Hoeller
 * @author Trevor D. Cook
 * @since 29.09.2003
 * @see MultipartHttpServletRequest
 * @see MultipartFile
 * @see org.springframework.web.multipart.commons.CommonsMultipartResolver
 * @see org.springframework.web.multipart.support.ByteArrayMultipartFileEditor
 * @see org.springframework.web.multipart.support.StringMultipartFileEditor
 * @see org.springframework.web.servlet.DispatcherServlet
 */
public interface MultipartResolver {

	/**
	 * 确定给定的请求是否为文件上传请求。
	 * <p>通常会检查内容类型“multipart / form-data”，但实际接受的请求可能取决于解析器实现的功能。
	 * @param request the servlet request to be evaluated
	 * @return whether the request contains multipart content
	 */
	boolean isMultipart(HttpServletRequest request);

	/**
	 * 将给定的HTTP请求解析为多部分文件和参数，
	 * 包装请求为提供访问文件描述符合可以通过标准ServletRequest方法访问参数的
	 * {@link org.springframework.web.multipart.MultipartHttpServletRequest}对象
	 * @param request the servlet request to wrap (must be of a multipart content type)
	 * @return the wrapped servlet request
	 * @throws MultipartException if the servlet request is not multipart, or if
	 * implementation-specific problems are encountered (such as exceeding file size limits)
	 * @see MultipartHttpServletRequest#getFile
	 * @see MultipartHttpServletRequest#getFileNames
	 * @see MultipartHttpServletRequest#getFileMap
	 * @see javax.servlet.http.HttpServletRequest#getParameter
	 * @see javax.servlet.http.HttpServletRequest#getParameterNames
	 * @see javax.servlet.http.HttpServletRequest#getParameterMap
	 */
	MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException;

	/**
	 * 清理用于文件上传的所有资源，就像上传文件的存储一样。
	 * @param request the request to cleanup resources for
	 */
	void cleanupMultipart(MultipartHttpServletRequest request);

}

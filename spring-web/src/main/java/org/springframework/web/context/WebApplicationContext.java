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

package org.springframework.web.context;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;

/**
 * Interface to provide configuration for a web application. This is read-only while
 * the application is running, but may be reloaded if the implementation supports this.
 *
 * 用于为Web应用程序提供配置的界面。 这在应用程序运行时是只读的，但如果实现支持，则可以重新加载
 *
 * <p>此接口将{@code getServletContext（）}方法添加到通用ApplicationContext接口，并添加一些大家都知道的
 * 根上下文必须在引导过程中必须绑定值到的属性名称
 *
 *
 * <p>与通用应用程序上下文一样，Web应用程序上下文是分层的.
 * 每个应用程序都有一个根上下文，而应用程序中的每个servlet（包括MVC框架中的调度程序servlet）都有自己的子上下文
 *
 * <p>除了标准的应用程序上下文生命周期功能外，
 * WebApplicationContext实现需要检测{@link ServletContextAware} bean并相应地调用{@code setServletContext}方法
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since January 19, 2001
 * @see ServletContextAware#setServletContext
 */
public interface WebApplicationContext extends ApplicationContext {

	/**
	 * Context attribute to bind root WebApplicationContext to on successful startup.
	 * <p>Note: If the startup of the root context fails, this attribute can contain
	 * an exception or error as value. Use WebApplicationContextUtils for convenient
	 * lookup of the root WebApplicationContext.
	 * @see org.springframework.web.context.support.WebApplicationContextUtils#getWebApplicationContext
	 * @see org.springframework.web.context.support.WebApplicationContextUtils#getRequiredWebApplicationContext
	 */
	String ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class.getName() + ".ROOT";

	/**
	 * Scope identifier for request scope: "request".
	 * Supported in addition to the standard scopes "singleton" and "prototype".
	 */
	String SCOPE_REQUEST = "request";

	/**
	 * Scope identifier for session scope: "session".
	 * Supported in addition to the standard scopes "singleton" and "prototype".
	 */
	String SCOPE_SESSION = "session";

	/**
	 * Scope identifier for global session scope: "globalSession".
	 * Supported in addition to the standard scopes "singleton" and "prototype".
	 */
	String SCOPE_GLOBAL_SESSION = "globalSession";

	/**
	 * Scope identifier for the global web application scope: "application".
	 * Supported in addition to the standard scopes "singleton" and "prototype".
	 */
	String SCOPE_APPLICATION = "application";

	/**
	 * Name of the ServletContext environment bean in the factory.
	 * @see javax.servlet.ServletContext
	 */
	String SERVLET_CONTEXT_BEAN_NAME = "servletContext";

	/**
	 * Name of the ServletContext/PortletContext init-params environment bean in the factory.
	 * <p>Note: Possibly merged with ServletConfig/PortletConfig parameters.
	 * ServletConfig parameters override ServletContext parameters of the same name.
	 * @see javax.servlet.ServletContext#getInitParameterNames()
	 * @see javax.servlet.ServletContext#getInitParameter(String)
	 * @see javax.servlet.ServletConfig#getInitParameterNames()
	 * @see javax.servlet.ServletConfig#getInitParameter(String)
	 */
	String CONTEXT_PARAMETERS_BEAN_NAME = "contextParameters";

	/**
	 * Name of the ServletContext/PortletContext attributes environment bean in the factory.
	 * @see javax.servlet.ServletContext#getAttributeNames()
	 * @see javax.servlet.ServletContext#getAttribute(String)
	 */
	String CONTEXT_ATTRIBUTES_BEAN_NAME = "contextAttributes";


	/**
	 * 返回此应用程序的标准Servlet API ServletContext。
	 * <p>Also available for a Portlet application, in addition to the PortletContext.
	 */
	ServletContext getServletContext();

}

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

package org.springframework.web.context;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * 由可配置的Web应用程序上下文实现的接口。
 * 由{@link ContextLoader}和{@link org.springframework.web.servlet.FrameworkServlet}提供支持.
 *
 * <p>Note: The setters of this interface need to be called before an
 * invocation of the {@link #refresh} method inherited from
 * {@link org.springframework.context.ConfigurableApplicationContext}.
 * They do not cause an initialization of the context on their own.
 *
 * <p>注意：在调用继承自{@link org.springframework.context.ConfigurableApplicationContext}
 * 的{@link #refresh}方法之前，需要调用此接口的setter。它们不会导致自己初始化上下文
 *
 * @author Juergen Hoeller
 * @since 05.12.2003
 * @see #refresh
 * @see ContextLoader#createWebApplicationContext
 * @see org.springframework.web.servlet.FrameworkServlet#createWebApplicationContext
 */
public interface ConfigurableWebApplicationContext extends WebApplicationContext, ConfigurableApplicationContext {

	/**
	 * Prefix for ApplicationContext ids that refer to context path and/or servlet name.
	 */
	String APPLICATION_CONTEXT_ID_PREFIX = WebApplicationContext.class.getName() + ":";

	/**
	 * Name of the ServletConfig environment bean in the factory.
	 * @see javax.servlet.ServletConfig
	 */
	String SERVLET_CONFIG_BEAN_NAME = "servletConfig";


	/**
	 * Set the ServletContext for this web application context.
	 *
	 * 为此Web应用程序上下文设置ServletContext。
	 * <p>Does not cause an initialization of the context: refresh needs to be
	 * called after the setting of all configuration properties.
	 * 不会导致上下文初始化：refresh需要在设置所有配置属性后调用
	 * @see #refresh()
	 */
	void setServletContext(ServletContext servletContext);

	/**
	 * Set the ServletConfig for this web application context.
	 * Only called for a WebApplicationContext that belongs to a specific Servlet.
	 *
	 * 为此Web应用程序上下文设置ServletConfig。
	 * @see #refresh()
	 */
	void setServletConfig(ServletConfig servletConfig);

	/**
	 * Return the ServletConfig for this web application context, if any.
	 *
	 */
	ServletConfig getServletConfig();

	/**
	 * 设置此Web应用程序上下文的命名空间，用于构建默认上下文配置位置。
	 * 根Web应用程序上下文没有命名空间。
	 */
	void setNamespace(String namespace);

	/**
	 * Return the namespace for this web application context, if any.
	 */
	String getNamespace();

	/**
	 * 以init-param样式设置此Web应用程序上下文的配置位置，即使用逗号，分号或空格分隔的不同位置
	 * <p>如果没有设置，则实现应该使用默认值
	 * 给定的命名空间或根Web应用程序上下文，视情况而定
	 */
	void setConfigLocation(String configLocation);

	/**
	 * Set the config locations for this web application context.
	 * <p>If not set, the implementation is supposed to use a default for the
	 * given namespace or the root web application context, as appropriate.
	 */
	void setConfigLocations(String... configLocations);

	/**
	 * Return the config locations for this web application context,
	 * or {@code null} if none specified.
	 */
	String[] getConfigLocations();

}

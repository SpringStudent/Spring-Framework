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

 * 基于web的主题解析策略接口，允许通过请求进行主题解析，并通过请求和相应进行修改
 *
 * 该接口允许基于session，cookie的实现；默认实现
 * {@link org.springframework.web.servlet.theme.FixedThemeResolver}
 * 只需使用配置的默认主题。
 *
 *
 * <p>请注意，此解析器仅负责确定当前主题名称。
 * 已解析主题名称的Theme实例由DispatcherServlet通过相应的ThemeSource查找，
 *   例如：当前的WebApplicationContext。
 *
 * <p>使用{@link org.springframework.web.servlet.support.RequestContext＃getTheme（）}
 * 检索控制器或视图中的当前主题，与实际解析策略无关。
 *
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @since 17.06.2003
 * @see org.springframework.ui.context.Theme
 * @see org.springframework.ui.context.ThemeSource
 */
public interface ThemeResolver {

	/**
	 * 通过给定的请求解析当前主题名称
	 * 在任何情况下都应该返回默认主题作为备用。
	 * @param request request to be used for resolution
	 * @return the current theme name
	 */
	String resolveThemeName(HttpServletRequest request);

	/**
	 * 将当前主题名称设置为给定的主题名称。
	 * @param request request to be used for theme name modification
	 * @param response response to be used for theme name modification
	 * @param themeName the new theme name
	 * @throws UnsupportedOperationException if the ThemeResolver implementation
	 * does not support dynamic changing of the theme
	 */
	void setThemeName(HttpServletRequest request, HttpServletResponse response, String themeName);

}

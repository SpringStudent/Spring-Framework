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

package org.springframework.web.servlet;

import java.util.Locale;

/**
 * 由可以按名称解析视图的对象实现的接口。
 *
 * <p>在运行应用程序期间，视图状态不会更改，所以实现可以自由地缓存视图。
 *
 * <p>Implementations are encouraged to support internationalization,
 * i.e. localized view resolution.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.servlet.view.InternalResourceViewResolver
 * @see org.springframework.web.servlet.view.ResourceBundleViewResolver
 * @see org.springframework.web.servlet.view.XmlViewResolver
 */
public interface ViewResolver {

	/**
	 * 按名称解析给定视图。
	 * <p>为了允许有ViewResolver链,viewResolver应该返回{@code null}如果给定名称的视图
	 * 未定义
	 * 然后，这并不是必要的:一些ViewResolvers将始终尝试
	 * 使用给定名称构建View对象，无法返回{@code null}
	 * （而在View创建失败时抛出异常）。
	 * @param viewName name of the view to resolve
	 * @param locale Locale in which to resolve the view.
	 * ViewResolvers that support internationalization should respect this.
	 * @return the View object, or {@code null} if not found
	 * (optional, to allow for ViewResolver chaining)
	 * @throws Exception if the view cannot be resolved
	 * (typically in case of problems creating an actual View object)
	 */
	View resolveViewName(String viewName, Locale locale) throws Exception;

}

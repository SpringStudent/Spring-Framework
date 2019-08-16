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

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * 基于web的区域设置解决策略接口允许通过请求进行区域设置解析，并通过请求和响应修改区域设置。
 *
 * <p>该接口允许基于请求，session，
 * cookies等。默认实现是
 * {@link org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver}，
 * 只需使用相应HTTP标头提供的请求的语言环境。

 * 使用Use {@link org.springframework.web.servlet.support.RequestContext#getLocale()}
 * 获取来检索控制器或视图中的当前区域设置，独立于实际的区域设置策略
 *
 * <p>注意：从Spring 4.0开始，有一个扩展的策略接口
 * 叫{@link LocaleContextResolver}，允许解析
 * 一个{@link org.springframework.context.i18n.LocaleContext}对象，
 * 可能包括相关的时区信息。Spring提供的解析器在适当的地方实现了扩展
 * {@link LocaleContextResolver}接口。
 *
 * @author Juergen Hoeller
 * @since 27.02.2003
 * @see LocaleContextResolver
 * @see org.springframework.context.i18n.LocaleContextHolder
 * @see org.springframework.web.servlet.support.RequestContext#getLocale
 * @see org.springframework.web.servlet.support.RequestContextUtils#getLocale
 */
public interface LocaleResolver {

	/**
	 * 通过给定请求解析当前区域设置。
	 * 在任何情况下都可以返回默认语言环境
	 * @param request the request to resolve the locale for
	 * @return the current locale (never {@code null})
	 */
	Locale resolveLocale(HttpServletRequest request);

	/**
	 * 将当前区域设置设置为给定的区域设置。
	 * @param request the request to be used for locale modification
	 * @param response the response to be used for locale modification
	 * @param locale the new locale, or {@code null} to clear the locale
	 * @throws UnsupportedOperationException if the LocaleResolver
	 * implementation does not support dynamic changing of the locale
	 */
	void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale);

}

/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.web.servlet.view;

import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * {@link org.springframework.web.servlet.ViewResolver}的简单实现，
 * 它将视图名称解释为当前应用程序上下文中的bean名称，
 * 即通常在执行{@code DispatcherServlet}的XML文件中。
 *
 * <p>这个解析器可以方便地用于小型应用程序，保持所有定义从控制器到视图在同一个地方。
 * 对于较大的应用程序，{@link XmlViewResolver}将是更好的选择，
 * 因为它将XML视图bean定义分离为专用的视图文件。
 *
 * <p>注意：这个{@code ViewResolver}和{@link XmlViewResolver}都不支持国际化。
 * 如果需要为每个区域设置应用不同的视图资源，请考虑{@link ResourceBundleViewResolver}。
 *
 * <p>注意：此{@code ViewResolver}实现了{@link Ordered}接口，以便灵活地参与{@code ViewResolver}链接。
 * 例如，可以通过此{@code ViewResolver}定义一些特殊视图（将其作为“order”值赋予0），
 * 而所有剩余视图可以通过{@link UrlBasedViewResolver}解析。
 *
 * @author Juergen Hoeller
 * @since 18.06.2003
 * @see XmlViewResolver
 * @see ResourceBundleViewResolver
 * @see UrlBasedViewResolver
 */
public class BeanNameViewResolver extends WebApplicationObjectSupport implements ViewResolver, Ordered {

	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered


	/**
	 * Specify the order value for this ViewResolver bean.
	 * <p>The default value is {@code Ordered.LOWEST_PRECEDENCE}, meaning non-ordered.
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}


	@Override
	public View resolveViewName(String viewName, Locale locale) throws BeansException {
		ApplicationContext context = getApplicationContext();
		if (!context.containsBean(viewName)) {
			if (logger.isDebugEnabled()) {
				logger.debug("No matching bean found for view name '" + viewName + "'");
			}
			// Allow for ViewResolver chaining...
			return null;
		}
		if (!context.isTypeMatch(viewName, View.class)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Found matching bean for view name '" + viewName +
						"' - to be ignored since it does not implement View");
			}
			// Since we're looking into the general ApplicationContext here,
			// let's accept this as a non-match and allow for chaining as well...
			return null;
		}
		return context.getBean(viewName, View.class);
	}

}

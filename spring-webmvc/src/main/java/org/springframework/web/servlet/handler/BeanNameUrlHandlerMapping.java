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

package org.springframework.web.servlet.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

/**
 *
 * 实现了从URL映射到名称以斜杠（“/”）开头的bean，类似于Struts将URL映射到操作名称的方式。
 * {@link org.springframework.web.servlet.HandlerMapping}接口的实现
 *
 * <p>当前类和{@link org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping}
 * 是使用{@link org.springframework.web.servlet.DispatcherServlet}时提供的默认handlerMapping
 * 或者，{@link SimpleUrlHandlerMapping}允许以声明方式自定义处理程序映射。 
 *
 *
 * <p>从URL到bean名称的映射. 因此，在多个映射到单个处理程序的情况下，
 * 传入的URL“/ foo”将映射到名为“/ foo”的处理程序或“/ foo / foo2”。
 *注意：在XML定义中，您需要在bean定义中使用别名name =“/ foo”，因为XML id可能不包含斜杠。
 *
 * <p>支持直接匹配（给定“/ test” - >注册“/ test”）和“*”
 *   匹配（给定“/ test” - >注册“/ t *”）。 请注意，默认情况下是在当前servlet映射中映射（如果适用）;
 * 有关详细信息，请参阅{@link #setAlwaysUseFullPath“alwaysUseFullPath”}属性。
 * 有关模式选项的详细信息，请参阅{@link org.springframework.util.AntPathMatcher} javadoc。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see SimpleUrlHandlerMapping
 */
public class BeanNameUrlHandlerMapping extends AbstractDetectingUrlHandlerMapping {

	/**
	 * 检查URL的给定bean的名称和别名，以“/”开头。
	 */
	@Override
	protected String[] determineUrlsForHandler(String beanName) {
		List<String> urls = new ArrayList<String>();
		if (beanName.startsWith("/")) {
			urls.add(beanName);
		}
		//获取bean的别名
		String[] aliases = getApplicationContext().getAliases(beanName);
		for (String alias : aliases) {
			if (alias.startsWith("/")) {
				urls.add(alias);
			}
		}
		//获取给定beanName的所有urls
		return StringUtils.toStringArray(urls);
	}

}

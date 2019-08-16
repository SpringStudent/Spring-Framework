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

package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;

/**
 * 由定义请求和处理程序对象之间的映射对象实现的接口。
 *
 * <p>这个类可以由应用程序开发人员实现，虽然这不是必需的，
 * 因为{@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}和
 * {@link org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping}是 包含在框架中。
 * 如果在应用程序上下文中未注册HandlerMapping bean，则前者是缺省值。
 *
 * <p>HandlerMapping实现可以支持映射的拦截器，但不必如此。
 * 处理器将会被包装为 {@link HandlerExecutionChain}实例,
 * 并可以附带一些{@link HandlerInterceptor}实例。
 * DispatcherServlet将首先按给定顺序排列调用每个HandlerInterceptor
 * {@code preHandle}方法，如果所有{@code preHandle}方法都返回{@code true}，则最终调用处理程序本身。
 *
 * <p>mapping提供的参数化能力是MVC框架的一个NB并且不同寻常的功能。
 * 例如，可以基于会话状态，cookie状态或许多其他变量编写自定义映射。
 * 其他MVC框架似乎没有这么灵活
 *
 * <p>可以实现{@link org.springframework.core.Ordered}接口，
 * 以便能够指定排序顺序，从而获得DispatcherServlet应用的优先级。 非有序实例被视为最低优先级。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.core.Ordered
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping
 * @see org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping
 * @see org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping
 */
public interface HandlerMapping {

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the mapped
	 * handler for the best matching pattern.
	 * @since 4.3.21
	 */
	String BEST_MATCHING_HANDLER_ATTRIBUTE = HandlerMapping.class.getName() + ".bestMatchingHandler";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the path
	 * within the handler mapping, in case of a pattern match, or the full
	 * relevant URI (typically within the DispatcherServlet's mapping) else.
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations. URL-based HandlerMappings will
	 * typically support it, but handlers should not necessarily expect
	 * this request attribute to be present in all scenarios.
	 */
	String PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE = HandlerMapping.class.getName() + ".pathWithinHandlerMapping";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the
	 * best matching pattern within the handler mapping.
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations. URL-based HandlerMappings will
	 * typically support it, but handlers should not necessarily expect
	 * this request attribute to be present in all scenarios.
	 */
	String BEST_MATCHING_PATTERN_ATTRIBUTE = HandlerMapping.class.getName() + ".bestMatchingPattern";

	/**
	 * Name of the boolean {@link HttpServletRequest} attribute that indicates
	 * whether type-level mappings should be inspected.
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations.
	 */
	String INTROSPECT_TYPE_LEVEL_MAPPING = HandlerMapping.class.getName() + ".introspectTypeLevelMapping";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the URI
	 * templates map, mapping variable names to values.
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations. URL-based HandlerMappings will
	 * typically support it, but handlers should not necessarily expect
	 * this request attribute to be present in all scenarios.
	 */
	String URI_TEMPLATE_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".uriTemplateVariables";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains a map with
	 * URI matrix variables.
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations and may also not be present depending on
	 * whether the HandlerMapping is configured to keep matrix variable content
	 * in the request URI.
	 */
	String MATRIX_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".matrixVariables";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the set of
	 * producible MediaTypes applicable to the mapped handler.
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations. Handlers should not necessarily expect
	 * this request attribute to be present in all scenarios.
	 */
	String PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE = HandlerMapping.class.getName() + ".producibleMediaTypes";

	/**
	 * 返回此请求的处理程序和所有拦截器.
	 * 可以根据请求URL，会话状态或实现类任何因素进行选择。
	 * <p>返回的HandlerExecutionChain包含一个处理程序Object，而不是一个标记接口，因此处理程序不会受到任何限制。
	 * 例如，可以编写HandlerAdapter以允许使用另一个框架的处理程序对象。
	 * <p>如果未找到匹配项，则返回{@code null}。 这不是错误。
	 * DispatcherServlet将查询所有已注册的HandlerMapping bean以查找匹配项，并且只有在没有找到处理程序时才会确定存在错误。
	 * @param request current HTTP request
	 * @return a HandlerExecutionChain instance containing handler object and
	 * any interceptors, or {@code null} if no mapping found
	 * @throws Exception if there is an internal error
	 */
	HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;

}

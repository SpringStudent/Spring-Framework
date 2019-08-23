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

package org.springframework.web.servlet.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UrlPathHelper;

/**
 * {@link org.springframework.web.servlet.HandlerMapping}实现的抽象基类。
 * 支持排序，默认处理程序，处理程序拦截器，包括由路径模式映射的处理程序拦截器。
 *
 * <p>注意：这个基类<i>不</ i>支持曝光
 *   {@link #PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE}。 对此属性的支持取决于具体的子类，通常基于请求URL映射。
 *
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 07.04.2003
 * @see #getHandlerInternal
 * @see #setDefaultHandler
 * @see #setAlwaysUseFullPath
 * @see #setUrlDecode
 * @see org.springframework.util.AntPathMatcher
 * @see #setInterceptors
 * @see org.springframework.web.servlet.HandlerInterceptor
 */
public abstract class AbstractHandlerMapping extends WebApplicationObjectSupport implements HandlerMapping, Ordered {

	private Object defaultHandler;

	private UrlPathHelper urlPathHelper = new UrlPathHelper();

	private PathMatcher pathMatcher = new AntPathMatcher();

	private final List<Object> interceptors = new ArrayList<Object>();

	private final List<HandlerInterceptor> adaptedInterceptors = new ArrayList<HandlerInterceptor>();

	private final UrlBasedCorsConfigurationSource globalCorsConfigSource = new UrlBasedCorsConfigurationSource();

	private CorsProcessor corsProcessor = new DefaultCorsProcessor();

	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered


	/**
	 * Set the default handler for this handler mapping.
	 * This handler will be returned if no specific mapping was found.
	 * <p>Default is {@code null}, indicating no default handler.
	 */
	public void setDefaultHandler(Object defaultHandler) {
		this.defaultHandler = defaultHandler;
	}

	/**
	 * 返回此处理程序映射的默认处理程序
	 * or {@code null} if none.
	 */
	public Object getDefaultHandler() {
		return this.defaultHandler;
	}

	/**
	 * Set if URL lookup should always use the full path within the current servlet
	 * context. Else, the path within the current servlet mapping is used if applicable
	 * (that is, in the case of a ".../*" servlet mapping in web.xml).
	 * <p>Default is "false".
	 * @see org.springframework.web.util.UrlPathHelper#setAlwaysUseFullPath
	 */
	public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
		this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
		this.globalCorsConfigSource.setAlwaysUseFullPath(alwaysUseFullPath);
	}

	/**
	 * Set if context path and request URI should be URL-decoded. Both are returned
	 * <i>undecoded</i> by the Servlet API, in contrast to the servlet path.
	 * <p>Uses either the request encoding or the default encoding according
	 * to the Servlet spec (ISO-8859-1).
	 * @see org.springframework.web.util.UrlPathHelper#setUrlDecode
	 */
	public void setUrlDecode(boolean urlDecode) {
		this.urlPathHelper.setUrlDecode(urlDecode);
		this.globalCorsConfigSource.setUrlDecode(urlDecode);
	}

	/**
	 * Set if ";" (semicolon) content should be stripped from the request URI.
	 * <p>The default value is {@code true}.
	 * @see org.springframework.web.util.UrlPathHelper#setRemoveSemicolonContent(boolean)
	 */
	public void setRemoveSemicolonContent(boolean removeSemicolonContent) {
		this.urlPathHelper.setRemoveSemicolonContent(removeSemicolonContent);
		this.globalCorsConfigSource.setRemoveSemicolonContent(removeSemicolonContent);
	}

	/**
	 * Set the UrlPathHelper to use for resolution of lookup paths.
	 * <p>Use this to override the default UrlPathHelper with a custom subclass,
	 * or to share common UrlPathHelper settings across multiple HandlerMappings
	 * and MethodNameResolvers.
	 */
	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
		this.urlPathHelper = urlPathHelper;
		this.globalCorsConfigSource.setUrlPathHelper(urlPathHelper);
	}

	/**
	 * Return the UrlPathHelper implementation to use for resolution of lookup paths.
	 */
	public UrlPathHelper getUrlPathHelper() {
		return urlPathHelper;
	}

	/**
	 * Set the PathMatcher implementation to use for matching URL paths
	 * against registered URL patterns. Default is AntPathMatcher.
	 * @see org.springframework.util.AntPathMatcher
	 */
	public void setPathMatcher(PathMatcher pathMatcher) {
		Assert.notNull(pathMatcher, "PathMatcher must not be null");
		this.pathMatcher = pathMatcher;
		this.globalCorsConfigSource.setPathMatcher(pathMatcher);
	}

	/**
	 * Return the PathMatcher implementation to use for matching URL paths
	 * against registered URL patterns.
	 */
	public PathMatcher getPathMatcher() {
		return this.pathMatcher;
	}

	/**
	 * Set the interceptors to apply for all handlers mapped by this handler mapping.
	 * <p>Supported interceptor types are HandlerInterceptor, WebRequestInterceptor, and MappedInterceptor.
	 * Mapped interceptors apply only to request URLs that match its path patterns.
	 * Mapped interceptor beans are also detected by type during initialization.
	 * @param interceptors array of handler interceptors
	 * @see #adaptInterceptor
	 * @see org.springframework.web.servlet.HandlerInterceptor
	 * @see org.springframework.web.context.request.WebRequestInterceptor
	 */
	public void setInterceptors(Object... interceptors) {
		this.interceptors.addAll(Arrays.asList(interceptors));
	}

	/**
	 * Set "global" CORS configuration based on URL patterns. By default the first
	 * matching URL pattern is combined with the CORS configuration for the
	 * handler, if any.
	 * @since 4.2
	 */
	public void setCorsConfigurations(Map<String, CorsConfiguration> corsConfigurations) {
		this.globalCorsConfigSource.setCorsConfigurations(corsConfigurations);
	}

	/**
	 * Get the "global" CORS configuration.
	 */
	public Map<String, CorsConfiguration> getCorsConfigurations() {
		return this.globalCorsConfigSource.getCorsConfigurations();
	}

	/**
	 * Configure a custom {@link CorsProcessor} to use to apply the matched
	 * {@link CorsConfiguration} for a request.
	 * <p>By default {@link DefaultCorsProcessor} is used.
	 * @since 4.2
	 */
	public void setCorsProcessor(CorsProcessor corsProcessor) {
		Assert.notNull(corsProcessor, "CorsProcessor must not be null");
		this.corsProcessor = corsProcessor;
	}

	/**
	 * Return the configured {@link CorsProcessor}.
	 */
	public CorsProcessor getCorsProcessor() {
		return this.corsProcessor;
	}

	/**
	 * Specify the order value for this HandlerMapping bean.
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


	/**
	 * Initializes the interceptors.
	 * @see #extendInterceptors(java.util.List)
	 * @see #initInterceptors()
	 */
	@Override
	protected void initApplicationContext() throws BeansException {
		extendInterceptors(this.interceptors);
		detectMappedInterceptors(this.adaptedInterceptors);
		initInterceptors();
	}

	/**
	 * Extension hook that subclasses can override to register additional interceptors,
	 * given the configured interceptors (see {@link #setInterceptors}).
	 * <p>Will be invoked before {@link #initInterceptors()} adapts the specified
	 * interceptors into {@link HandlerInterceptor} instances.
	 * <p>The default implementation is empty.
	 * @param interceptors the configured interceptor List (never {@code null}), allowing
	 * to add further interceptors before as well as after the existing interceptors
	 */
	protected void extendInterceptors(List<Object> interceptors) {
	}

	/**
	 * Detect beans of type {@link MappedInterceptor} and add them to the list of mapped interceptors.
	 * <p>This is called in addition to any {@link MappedInterceptor}s that may have been provided
	 * via {@link #setInterceptors}, by default adding all beans of type {@link MappedInterceptor}
	 * from the current context and its ancestors. Subclasses can override and refine this policy.
	 * @param mappedInterceptors an empty list to add {@link MappedInterceptor} instances to
	 */
	protected void detectMappedInterceptors(List<HandlerInterceptor> mappedInterceptors) {
		mappedInterceptors.addAll(
				BeanFactoryUtils.beansOfTypeIncludingAncestors(
						getApplicationContext(), MappedInterceptor.class, true, false).values());
	}

	/**
	 * Initialize the specified interceptors, checking for {@link MappedInterceptor}s and
	 * adapting {@link HandlerInterceptor}s and {@link WebRequestInterceptor}s if necessary.
	 * @see #setInterceptors
	 * @see #adaptInterceptor
	 */
	protected void initInterceptors() {
		if (!this.interceptors.isEmpty()) {
			for (int i = 0; i < this.interceptors.size(); i++) {
				Object interceptor = this.interceptors.get(i);
				if (interceptor == null) {
					throw new IllegalArgumentException("Entry number " + i + " in interceptors array is null");
				}
				this.adaptedInterceptors.add(adaptInterceptor(interceptor));
			}
		}
	}

	/**
	 * Adapt the given interceptor object to the {@link HandlerInterceptor} interface.
	 * <p>By default, the supported interceptor types are {@link HandlerInterceptor}
	 * and {@link WebRequestInterceptor}. Each given {@link WebRequestInterceptor}
	 * will be wrapped in a {@link WebRequestHandlerInterceptorAdapter}.
	 * Can be overridden in subclasses.
	 * @param interceptor the specified interceptor object
	 * @return the interceptor wrapped as HandlerInterceptor
	 * @see org.springframework.web.servlet.HandlerInterceptor
	 * @see org.springframework.web.context.request.WebRequestInterceptor
	 * @see WebRequestHandlerInterceptorAdapter
	 */
	protected HandlerInterceptor adaptInterceptor(Object interceptor) {
		if (interceptor instanceof HandlerInterceptor) {
			return (HandlerInterceptor) interceptor;
		}
		else if (interceptor instanceof WebRequestInterceptor) {
			return new WebRequestHandlerInterceptorAdapter((WebRequestInterceptor) interceptor);
		}
		else {
			throw new IllegalArgumentException("Interceptor type not supported: " + interceptor.getClass().getName());
		}
	}

	/**
	 * Return the adapted interceptors as {@link HandlerInterceptor} array.
	 * @return the array of {@link HandlerInterceptor}s, or {@code null} if none
	 */
	protected final HandlerInterceptor[] getAdaptedInterceptors() {
		int count = this.adaptedInterceptors.size();
		return (count > 0 ? this.adaptedInterceptors.toArray(new HandlerInterceptor[count]) : null);
	}

	/**
	 * Return all configured {@link MappedInterceptor}s as an array.
	 * @return the array of {@link MappedInterceptor}s, or {@code null} if none
	 */
	protected final MappedInterceptor[] getMappedInterceptors() {
		List<MappedInterceptor> mappedInterceptors = new ArrayList<MappedInterceptor>(this.adaptedInterceptors.size());
		for (HandlerInterceptor interceptor : this.adaptedInterceptors) {
			if (interceptor instanceof MappedInterceptor) {
				mappedInterceptors.add((MappedInterceptor) interceptor);
			}
		}
		int count = mappedInterceptors.size();
		return (count > 0 ? mappedInterceptors.toArray(new MappedInterceptor[count]) : null);
	}


	/**
	 * 查找给定请求的handler,如果没有找到，回退到默认值
	 *
	 * @param request current HTTP request
	 * @return the corresponding handler instance, or the default handler
	 * @see #getHandlerInternal
	 */
	@Override
	public final HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		//获取request的处理程序Handler
		Object handler = getHandlerInternal(request);
		if (handler == null) {
			//返回默认的Handler
			handler = getDefaultHandler();
		}
		if (handler == null) {
			return null;
		}
		// Bean名称或已解析的处理程序
		if (handler instanceof String) {
			//如果找到的handler是个bean名称
			String handlerName = (String) handler;
			handler = getApplicationContext().getBean(handlerName);
		}
		HandlerExecutionChain executionChain = getHandlerExecutionChain(handler, request);
		if (CorsUtils.isCorsRequest(request)) {
			//全局cors配置
			CorsConfiguration globalConfig = this.globalCorsConfigSource.getCorsConfiguration(request);
			CorsConfiguration handlerConfig = getCorsConfiguration(handler, request);
			CorsConfiguration config = (globalConfig != null ? globalConfig.combine(handlerConfig) : handlerConfig);
			executionChain = getCorsHandlerExecutionChain(request, executionChain, config);
		}
		return executionChain;
	}

	/**
	 * Look up a handler for the given request, returning {@code null} if no
	 * specific one is found. This method is called by {@link #getHandler};
	 * a {@code null} return value will lead to the default handler, if one is set.
	 * <p>On CORS pre-flight requests this method should return a match not for
	 * the pre-flight request but for the expected actual request based on the URL
	 * path, the HTTP methods from the "Access-Control-Request-Method" header, and
	 * the headers from the "Access-Control-Request-Headers" header thus allowing
	 * the CORS configuration to be obtained via {@link #getCorsConfigurations},
	 * <p>Note: This method may also return a pre-built {@link HandlerExecutionChain},
	 * combining a handler object with dynamically determined interceptors.
	 * Statically specified interceptors will get merged into such an existing chain.
	 * @param request current HTTP request
	 * @return the corresponding handler instance, or {@code null} if none found
	 * @throws Exception if there is an internal error
	 */
	protected abstract Object getHandlerInternal(HttpServletRequest request) throws Exception;

	/**
	 * 为给定的处理程序构建{@link HandlerExecutionChain}，包括
	 *   适用的拦截器。
	 * <p>默认实现使用给定的处理程序，处理程序映射的公共拦截器以及与当前请求URL
	 * 匹配的任何{@link MappedInterceptor}构建标准{@link HandlerExecutionChain}。
	 * 拦截器按其注册顺序添加。 子类可以覆盖它以扩展/重新排列拦截器列表。
	 * <p><b>注意：</ b>传入的处理程序对象可以是原始处理程序或预构建的{@link HandlerExecutionChain}。
	 * 这种方法应该明确地处理这两种情况，要么建立一个新的{@link HandlerExecutionChain}，要么扩展现有的链。
	 * <p>要简单地在自定义子类中添加拦截器，请考虑调用{@code super.getHandlerExecutionChain（handler，request）}
	 * 并在返回的链对象上调用{@link HandlerExecutionChain＃addInterceptor}。
	 * @param handler the resolved handler instance (never {@code null})
	 * @param request current HTTP request
	 * @return the HandlerExecutionChain (never {@code null})
	 * @see #getAdaptedInterceptors()
	 */
	protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
		//handler可能为HandlerExecutionChain类型
		HandlerExecutionChain chain = (handler instanceof HandlerExecutionChain ?
				(HandlerExecutionChain) handler : new HandlerExecutionChain(handler));
		String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);
		for (HandlerInterceptor interceptor : this.adaptedInterceptors) {
			//mappedInterceptor类型需要
			if (interceptor instanceof MappedInterceptor) {
				MappedInterceptor mappedInterceptor = (MappedInterceptor) interceptor;
				//给定路径匹配成功
				if (mappedInterceptor.matches(lookupPath, this.pathMatcher)) {
					chain.addInterceptor(mappedInterceptor.getInterceptor());
				}
			}
			else {
				chain.addInterceptor(interceptor);
			}
		}
		return chain;
	}

	/**
	 * 检索给定处理程序的CORS配置。
	 * @param handler the handler to check (never {@code null}).
	 * @param request the current request.
	 * @return the CORS configuration for the handler, or {@code null} if none
	 * @since 4.2
	 */
	protected CorsConfiguration getCorsConfiguration(Object handler, HttpServletRequest request) {
		Object resolvedHandler = handler;
		if (handler instanceof HandlerExecutionChain) {
			resolvedHandler = ((HandlerExecutionChain) handler).getHandler();
		}
		if (resolvedHandler instanceof CorsConfigurationSource) {
			return ((CorsConfigurationSource) resolvedHandler).getCorsConfiguration(request);
		}
		return null;
	}

	/**
	 * 更新HandlerExecutionChain以进行与CORS相关的处理。
	 * <p>For pre-flight requests, the default implementation replaces the selected
	 * handler with a simple HttpRequestHandler that invokes the configured
	 * {@link #setCorsProcessor}.
	 * <p>For actual requests, the default implementation inserts a
	 * HandlerInterceptor that makes CORS-related checks and adds CORS headers.
	 * @param request the current request
	 * @param chain the handler chain
	 * @param config the applicable CORS configuration (possibly {@code null})
	 * @since 4.2
	 */
	protected HandlerExecutionChain getCorsHandlerExecutionChain(HttpServletRequest request,
			HandlerExecutionChain chain, CorsConfiguration config) {

		if (CorsUtils.isPreFlightRequest(request)) {
			HandlerInterceptor[] interceptors = chain.getInterceptors();
			chain = new HandlerExecutionChain(new PreFlightHandler(config), interceptors);
		}
		else {
			chain.addInterceptor(new CorsInterceptor(config));
		}
		return chain;
	}


	private class PreFlightHandler implements HttpRequestHandler, CorsConfigurationSource {

		private final CorsConfiguration config;

		public PreFlightHandler(CorsConfiguration config) {
			this.config = config;
		}

		@Override
		public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
			corsProcessor.processRequest(this.config, request, response);
		}

		@Override
		public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
			return this.config;
		}
	}


	private class CorsInterceptor extends HandlerInterceptorAdapter implements CorsConfigurationSource {

		private final CorsConfiguration config;

		public CorsInterceptor(CorsConfiguration config) {
			this.config = config;
		}

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
				throws Exception {

			return corsProcessor.processRequest(this.config, request, response);
		}

		@Override
		public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
			return this.config;
		}
	}

}

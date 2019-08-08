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

package org.springframework.test.web.servlet.setup;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.ServletContext;

import org.springframework.mock.web.MockServletConfig;
import org.springframework.test.web.servlet.DispatcherServletCustomizer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilderSupport;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.ConfigurableSmartRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * An abstract implementation of {@link org.springframework.test.web.servlet.MockMvcBuilder}
 * with common methods for configuring filters, default request properties, global
 * expectations and global result actions.
 * <p>
 * Sub-classes can use different strategies to prepare a WebApplicationContext to
 * pass to the DispatcherServlet.
 *
 * @author Rossen Stoyanchev
 * @author Stephane Nicoll
 * @since 4.0
 */
public abstract class AbstractMockMvcBuilder<B extends AbstractMockMvcBuilder<B>>
		extends MockMvcBuilderSupport implements ConfigurableMockMvcBuilder<B> {

	private List<Filter> filters = new ArrayList<Filter>();

	private RequestBuilder defaultRequestBuilder;

	private final List<ResultMatcher> globalResultMatchers = new ArrayList<ResultMatcher>();

	private final List<ResultHandler> globalResultHandlers = new ArrayList<ResultHandler>();

	private final List<DispatcherServletCustomizer> dispatcherServletCustomizers = new ArrayList<DispatcherServletCustomizer>();

	private final List<MockMvcConfigurer> configurers = new ArrayList<MockMvcConfigurer>(4);


	@SuppressWarnings("unchecked")
	public final <T extends B> T addFilters(Filter... filters) {
		Assert.notNull(filters, "filters cannot be null");

		for (Filter f : filters) {
			Assert.notNull(f, "filters cannot contain null values");
			this.filters.add(f);
		}
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public final <T extends B> T addFilter(Filter filter, String... urlPatterns) {

		Assert.notNull(filter, "filter cannot be null");
		Assert.notNull(urlPatterns, "urlPatterns cannot be null");

		if (urlPatterns.length > 0) {
			filter = new PatternMappingFilterProxy(filter, urlPatterns);
		}

		this.filters.add(filter);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public final <T extends B> T defaultRequest(RequestBuilder requestBuilder) {
		this.defaultRequestBuilder = requestBuilder;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public final <T extends B> T alwaysExpect(ResultMatcher resultMatcher) {
		this.globalResultMatchers.add(resultMatcher);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public final <T extends B> T alwaysDo(ResultHandler resultHandler) {
		this.globalResultHandlers.add(resultHandler);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public final <T extends B> T addDispatcherServletCustomizer(DispatcherServletCustomizer customizer) {
		this.dispatcherServletCustomizers.add(customizer);
		return (T) this;
	}

	public final <T extends B> T dispatchOptions(final boolean dispatchOptions) {
		return addDispatcherServletCustomizer(new DispatcherServletCustomizer() {
			@Override
			public void customize(DispatcherServlet dispatcherServlet) {
				dispatcherServlet.setDispatchOptionsRequest(dispatchOptions);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public final <T extends B> T apply(MockMvcConfigurer configurer) {
		configurer.afterConfigurerAdded(this);
		this.configurers.add(configurer);
		return (T) this;
	}


	/**
	 * Build a {@link org.springframework.test.web.servlet.MockMvc} instance.
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public final MockMvc build() {

		WebApplicationContext wac = initWebAppContext();

		ServletContext servletContext = wac.getServletContext();
		MockServletConfig mockServletConfig = new MockServletConfig(servletContext);

		for (MockMvcConfigurer configurer : this.configurers) {
			RequestPostProcessor processor = configurer.beforeMockMvcCreated(this, wac);
			if (processor != null) {
				if (this.defaultRequestBuilder == null) {
					this.defaultRequestBuilder = MockMvcRequestBuilders.get("/");
				}
				if (this.defaultRequestBuilder instanceof ConfigurableSmartRequestBuilder) {
					((ConfigurableSmartRequestBuilder) this.defaultRequestBuilder).with(processor);
				}
			}
		}

		Filter[] filterArray = this.filters.toArray(new Filter[this.filters.size()]);

		return super.createMockMvc(filterArray, mockServletConfig, wac, this.defaultRequestBuilder,
				this.globalResultMatchers, this.globalResultHandlers, this.dispatcherServletCustomizers);
	}

	/**
	 * A method to obtain the WebApplicationContext to be passed to the DispatcherServlet.
	 * Invoked from {@link #build()} before the
	 * {@link org.springframework.test.web.servlet.MockMvc} instance is created.
	 */
	protected abstract WebApplicationContext initWebAppContext();

}
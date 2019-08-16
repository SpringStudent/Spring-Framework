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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.View;

/**
 * 一个{@link org.springframework.web.servlet.ViewResolver}实现，
 * 它使用由bundle basename指定的{@link ResourceBundle}中的bean定义。
 *
 * <p>捆绑包通常在位于类路径中的属性文件中定义。 默认的bundle basename是“views”。
 *
 * <p>这个{@code ViewResolver}使用{@link java.util.PropertyResourceBundle}的默认支持来支持本地化视图定义。
 *  例如，对于给定的Locale“de_AT”，基本名称“views”将被解析为类路径资源“views_de_AT.properties”，
 *  “views_de.properties”，“views.properties”。
 *
 * <p>注意：此{@code ViewResolver}实现了{@link Ordered}接口，以便灵活地参与{@code ViewResolver}链接。
 * 例如，可以通过此{@code ViewResolver}定义一些特殊视图（将其作为“order”值赋予0），
 * 而所有剩余视图可以通过{@link UrlBasedViewResolver}解析。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see java.util.ResourceBundle#getBundle
 * @see java.util.PropertyResourceBundle
 * @see UrlBasedViewResolver
 */
public class ResourceBundleViewResolver extends AbstractCachingViewResolver
		implements Ordered, InitializingBean, DisposableBean {

	/** The default basename if no other basename is supplied */
	public static final String DEFAULT_BASENAME = "views";


	private String[] basenames = new String[] {DEFAULT_BASENAME};

	private ClassLoader bundleClassLoader = Thread.currentThread().getContextClassLoader();

	private String defaultParentView;

	private Locale[] localesToInitialize;

	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered

	/* Locale -> BeanFactory */
	private final Map<Locale, BeanFactory> localeCache =
			new HashMap<Locale, BeanFactory>();

	/* List of ResourceBundle -> BeanFactory */
	private final Map<List<ResourceBundle>, ConfigurableApplicationContext> bundleCache =
			new HashMap<List<ResourceBundle>, ConfigurableApplicationContext>();


	/**
	 * Set a single basename, following {@link java.util.ResourceBundle} conventions.
	 * The default is "views".
	 * <p>{@code ResourceBundle} supports different locale suffixes. For example,
	 * a base name of "views" might map to {@code ResourceBundle} files
	 * "views", "views_en_au" and "views_de".
	 * <p>Note that ResourceBundle names are effectively classpath locations: As a
	 * consequence, the JDK's standard ResourceBundle treats dots as package separators.
	 * This means that "test.theme" is effectively equivalent to "test/theme",
	 * just like it is for programmatic {@code java.util.ResourceBundle} usage.
	 * @see #setBasenames
	 * @see ResourceBundle#getBundle(String)
	 * @see ResourceBundle#getBundle(String, Locale)
	 */
	public void setBasename(String basename) {
		setBasenames(basename);
	}

	/**
	 * Set an array of basenames, each following {@link java.util.ResourceBundle}
	 * conventions. The default is a single basename "views".
	 * <p>{@code ResourceBundle} supports different locale suffixes. For example,
	 * a base name of "views" might map to {@code ResourceBundle} files
	 * "views", "views_en_au" and "views_de".
	 * <p>The associated resource bundles will be checked sequentially when resolving
	 * a message code. Note that message definitions in a <i>previous</i> resource
	 * bundle will override ones in a later bundle, due to the sequential lookup.
	 * <p>Note that ResourceBundle names are effectively classpath locations: As a
	 * consequence, the JDK's standard ResourceBundle treats dots as package separators.
	 * This means that "test.theme" is effectively equivalent to "test/theme",
	 * just like it is for programmatic {@code java.util.ResourceBundle} usage.
	 * @see #setBasename
	 * @see ResourceBundle#getBundle(String)
	 * @see ResourceBundle#getBundle(String, Locale)
	 */
	public void setBasenames(String... basenames) {
		this.basenames = basenames;
	}

	/**
	 * Set the {@link ClassLoader} to load resource bundles with.
	 * Default is the thread context {@code ClassLoader}.
	 */
	public void setBundleClassLoader(ClassLoader classLoader) {
		this.bundleClassLoader = classLoader;
	}

	/**
	 * Return the {@link ClassLoader} to load resource bundles with.
	 * <p>Default is the specified bundle {@code ClassLoader},
	 * usually the thread context {@code ClassLoader}.
	 */
	protected ClassLoader getBundleClassLoader() {
		return this.bundleClassLoader;
	}

	/**
	 * Set the default parent for views defined in the {@code ResourceBundle}.
	 * <p>This avoids repeated "yyy1.(parent)=xxx", "yyy2.(parent)=xxx" definitions
	 * in the bundle, especially if all defined views share the same parent.
	 * <p>The parent will typically define the view class and common attributes.
	 * Concrete views might simply consist of an URL definition then:
	 * a la "yyy1.url=/my.jsp", "yyy2.url=/your.jsp".
	 * <p>View definitions that define their own parent or carry their own
	 * class can still override this. Strictly speaking, the rule that a
	 * default parent setting does not apply to a bean definition that
	 * carries a class is there for backwards compatibility reasons.
	 * It still matches the typical use case.
	 */
	public void setDefaultParentView(String defaultParentView) {
		this.defaultParentView = defaultParentView;
	}

	/**
	 * Specify Locales to initialize eagerly, rather than lazily when actually accessed.
	 * <p>Allows for pre-initialization of common Locales, eagerly checking
	 * the view configuration for those Locales.
	 */
	public void setLocalesToInitialize(Locale... localesToInitialize) {
		this.localesToInitialize = localesToInitialize;
	}

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

	/**
	 * Eagerly initialize Locales if necessary.
	 * @see #setLocalesToInitialize
	 */
	@Override
	public void afterPropertiesSet() throws BeansException {
		if (this.localesToInitialize != null) {
			for (Locale locale : this.localesToInitialize) {
				initFactory(locale);
			}
		}
	}


	@Override
	protected View loadView(String viewName, Locale locale) throws Exception {
		BeanFactory factory = initFactory(locale);
		try {
			return factory.getBean(viewName, View.class);
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Allow for ViewResolver chaining...
			return null;
		}
	}

	/**
	 * Initialize the View {@link BeanFactory} from the {@code ResourceBundle},
	 * for the given {@link Locale locale}.
	 * <p>Synchronized because of access by parallel threads.
	 * @param locale the target {@code Locale}
	 * @return the View factory for the given Locale
	 * @throws BeansException in case of initialization errors
	 */
	protected synchronized BeanFactory initFactory(Locale locale) throws BeansException {
		// Try to find cached factory for Locale:
		// Have we already encountered that Locale before?
		if (isCache()) {
			BeanFactory cachedFactory = this.localeCache.get(locale);
			if (cachedFactory != null) {
				return cachedFactory;
			}
		}

		// Build list of ResourceBundle references for Locale.
		List<ResourceBundle> bundles = new LinkedList<ResourceBundle>();
		for (String basename : this.basenames) {
			ResourceBundle bundle = getBundle(basename, locale);
			bundles.add(bundle);
		}

		// Try to find cached factory for ResourceBundle list:
		// even if Locale was different, same bundles might have been found.
		if (isCache()) {
			BeanFactory cachedFactory = this.bundleCache.get(bundles);
			if (cachedFactory != null) {
				this.localeCache.put(locale, cachedFactory);
				return cachedFactory;
			}
		}

		// Create child ApplicationContext for views.
		GenericWebApplicationContext factory = new GenericWebApplicationContext();
		factory.setParent(getApplicationContext());
		factory.setServletContext(getServletContext());

		// Load bean definitions from resource bundle.
		PropertiesBeanDefinitionReader reader = new PropertiesBeanDefinitionReader(factory);
		reader.setDefaultParentBean(this.defaultParentView);
		for (ResourceBundle bundle : bundles) {
			reader.registerBeanDefinitions(bundle);
		}

		factory.refresh();

		// Cache factory for both Locale and ResourceBundle list.
		if (isCache()) {
			this.localeCache.put(locale, factory);
			this.bundleCache.put(bundles, factory);
		}

		return factory;
	}

	/**
	 * Obtain the resource bundle for the given basename and {@link Locale}.
	 * @param basename the basename to look for
	 * @param locale the {@code Locale} to look for
	 * @return the corresponding {@code ResourceBundle}
	 * @throws MissingResourceException if no matching bundle could be found
	 * @see ResourceBundle#getBundle(String, Locale, ClassLoader)
	 */
	protected ResourceBundle getBundle(String basename, Locale locale) throws MissingResourceException {
		return ResourceBundle.getBundle(basename, locale, getBundleClassLoader());
	}


	/**
	 * Close the bundle View factories on context shutdown.
	 */
	@Override
	public void destroy() throws BeansException {
		for (ConfigurableApplicationContext factory : this.bundleCache.values()) {
			factory.close();
		}
		this.localeCache.clear();
		this.bundleCache.clear();
	}

}

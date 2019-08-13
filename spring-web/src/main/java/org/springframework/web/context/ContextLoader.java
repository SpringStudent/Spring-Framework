/*
 * Copyright 2002-2017 the original author or authors.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 执行根应用程序上下文的实际初始化工作。 由{@link ContextLoaderListener}调用
 *
 * <p>在{@code web.xml} context-param级别查找{@link #CONTEXT_CLASS_PARAM“contextClass”}参数以指定上下文类类型，
 * 如果没有找到返回{@link org.springframework.web.context.support.XmlWebApplicationContext}
 * With the default ContextLoader implementation, any context class
 * specified needs to implement the {@link ConfigurableWebApplicationContext} interface.
 *
 *
 * <p>处理{@link #CONFIG_LOCATION_PARAM“contextConfigLocation”} context-param并将其值传递给上下文实例，
 * 将其解析为可能的多个文件路径，这些路径可以由任意数量的逗号和空格分隔，
 * 例如， “WEB-INF / applicationContext1.xml，WEB-INF / applicationContext2.xml”。
 * 也支持Ant样式的路径模式，例如， “WEB-INF / * Context.xml，WEB-INF / spring * .xml”或“WEB-INF /＆＃42;＆＃42; / * Context.xml”
 * Ant-style path patterns are supported as well, e.g.
 * "WEB-INF/*Context.xml,WEB-INF/spring*.xml" or "WEB-INF/&#42;&#42;/*Context.xml".
 * 如果没有明确指定，context implementation应该使用默认位置的
 * (with XmlWebApplicationContext: "/WEB-INF/applicationContext.xml")
 *
 * <p>注意：如果有多个配置位置，后面的bean定义会
 * 覆盖先前加载的文件中定义的文件，至少在使用Spring的默认ApplicationContext实现时。
 * 这使我们可以通过额外的XML文件故意覆盖某些bean定义。
 *
 * <p>除了加载根应用程序上下文之外，此类还可以选择加载或获取共享父上下文并将其挂接到根应用程序上下文
 * 有关更多信息，请参阅{@link #loadParentContext(ServletContext)}方法。
 *
 * <p>>从Spring 3.1开始，{@code ContextLoader}支持通过{@link #ContextLoader(WebApplicationContext)}
 * 构造函数注入根Web应用程序上下文，允许在Servlet 3.0+环境中进行编程配置
 * See {@link org.springframework.web.WebApplicationInitializer} for usage examples.
 *
 * @author Juergen Hoeller
 * @author Colin Sampaleanu
 * @author Sam Brannen
 * @since 17.02.2003
 * @see ContextLoaderListener
 * @see ConfigurableWebApplicationContext
 * @see org.springframework.web.context.support.XmlWebApplicationContext
 */
public class ContextLoader {

	/**
	 * Config param for the root WebApplicationContext id,
	 * to be used as serialization id for the underlying BeanFactory: {@value}
	 */
	public static final String CONTEXT_ID_PARAM = "contextId";

	/**
	 * Name of servlet context parameter (i.e., {@value}) that can specify the
	 * config location for the root context, falling back to the implementation's
	 * default otherwise.
	 * @see org.springframework.web.context.support.XmlWebApplicationContext#DEFAULT_CONFIG_LOCATION
	 */
	public static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";

	/**
	 * Config param for the root WebApplicationContext implementation class to use: {@value}
	 * @see #determineContextClass(ServletContext)
	 */
	public static final String CONTEXT_CLASS_PARAM = "contextClass";

	/**
	 * Config param for {@link ApplicationContextInitializer} classes to use
	 * for initializing the root web application context: {@value}
	 * @see #customizeContext(ServletContext, ConfigurableWebApplicationContext)
	 */
	public static final String CONTEXT_INITIALIZER_CLASSES_PARAM = "contextInitializerClasses";

	/**
	 * Config param for global {@link ApplicationContextInitializer} classes to use
	 * for initializing all web application contexts in the current application: {@value}
	 * @see #customizeContext(ServletContext, ConfigurableWebApplicationContext)
	 */
	public static final String GLOBAL_INITIALIZER_CLASSES_PARAM = "globalInitializerClasses";

	/**
	 * Optional servlet context parameter (i.e., "{@code locatorFactorySelector}")
	 * used only when obtaining a parent context using the default implementation
	 * of {@link #loadParentContext(ServletContext servletContext)}.
	 * Specifies the 'selector' used in the
	 * {@link ContextSingletonBeanFactoryLocator#getInstance(String selector)}
	 * method call, which is used to obtain the BeanFactoryLocator instance from
	 * which the parent context is obtained.
	 * <p>The default is {@code classpath*:beanRefContext.xml},
	 * matching the default applied for the
	 * {@link ContextSingletonBeanFactoryLocator#getInstance()} method.
	 * Supplying the "parentContextKey" parameter is sufficient in this case.
	 */
	public static final String LOCATOR_FACTORY_SELECTOR_PARAM = "locatorFactorySelector";

	/**
	 * Optional servlet context parameter (i.e., "{@code parentContextKey}")
	 * used only when obtaining a parent context using the default implementation
	 * of {@link #loadParentContext(ServletContext servletContext)}.
	 * Specifies the 'factoryKey' used in the
	 * {@link BeanFactoryLocator#useBeanFactory(String factoryKey)} method call,
	 * obtaining the parent application context from the BeanFactoryLocator instance.
	 * <p>Supplying this "parentContextKey" parameter is sufficient when relying
	 * on the default {@code classpath*:beanRefContext.xml} selector for
	 * candidate factory references.
	 */
	public static final String LOCATOR_FACTORY_KEY_PARAM = "parentContextKey";

	/**
	 * Any number of these characters are considered delimiters between
	 * multiple values in a single init-param String value.
	 */
	private static final String INIT_PARAM_DELIMITERS = ",; \t\n";

	/**
	 * Name of the class path resource (relative to the ContextLoader class)
	 * that defines ContextLoader's default strategy names.
	 */
	private static final String DEFAULT_STRATEGIES_PATH = "ContextLoader.properties";


	private static final Properties defaultStrategies;

	static {
		// Load default strategy implementations from properties file.
		// This is currently strictly internal and not meant to be customized
		// by application developers.
		// 从ContextLoader.properties加载默认的webApplicationContext策略
		try {
			ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, ContextLoader.class);
			//加载webApplicationContext的className
			defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not load 'ContextLoader.properties': " + ex.getMessage());
		}
	}


	/**
	 * Map from (thread context) ClassLoader to corresponding 'current' WebApplicationContext.
	 * key-thread context 类加载器    value-当前的webApplicationContext
	 */
	private static final Map<ClassLoader, WebApplicationContext> currentContextPerThread =
			new ConcurrentHashMap<ClassLoader, WebApplicationContext>(1);

	/**
	 * “当前”WebApplicationContext，如果是ContextLoader类的话
	 *   部署在Web应用程序ClassLoader本身。
	 */
	private static volatile WebApplicationContext currentContext;


	/**
	 * The root WebApplicationContext instance that this loader manages.
	 */
	private WebApplicationContext context;

	/**
	 * Holds BeanFactoryReference when loading parent factory via
	 * ContextSingletonBeanFactoryLocator.
	 */
	private BeanFactoryReference parentContextRef;

	/** Actual ApplicationContextInitializer instances to apply to the context */
	private final List<ApplicationContextInitializer<ConfigurableApplicationContext>> contextInitializers =
			new ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>>();


	/**
	 * Create a new {@code ContextLoader} that will create a web application context
	 * based on the "contextClass" and "contextConfigLocation" servlet context-params.
	 * See class-level documentation for details on default values for each.
	 * <p>This constructor is typically used when declaring the {@code
	 * ContextLoaderListener} subclass as a {@code <listener>} within {@code web.xml}, as
	 * a no-arg constructor is required.
	 * <p>The created application context will be registered into the ServletContext under
	 * the attribute name {@link WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE}
	 * and subclasses are free to call the {@link #closeWebApplicationContext} method on
	 * container shutdown to close the application context.
	 * @see #ContextLoader(WebApplicationContext)
	 * @see #initWebApplicationContext(ServletContext)
	 * @see #closeWebApplicationContext(ServletContext)
	 */
	public ContextLoader() {
	}

	/**
	 * Create a new {@code ContextLoader} with the given application context. This
	 * constructor is useful in Servlet 3.0+ environments where instance-based
	 * registration of listeners is possible through the {@link ServletContext#addListener}
	 * API.
	 * <p>The context may or may not yet be {@linkplain
	 * ConfigurableApplicationContext#refresh() refreshed}. If it (a) is an implementation
	 * of {@link ConfigurableWebApplicationContext} and (b) has <strong>not</strong>
	 * already been refreshed (the recommended approach), then the following will occur:
	 * <ul>
	 * <li>If the given context has not already been assigned an {@linkplain
	 * ConfigurableApplicationContext#setId id}, one will be assigned to it</li>
	 * <li>{@code ServletContext} and {@code ServletConfig} objects will be delegated to
	 * the application context</li>
	 * <li>{@link #customizeContext} will be called</li>
	 * <li>Any {@link ApplicationContextInitializer}s specified through the
	 * "contextInitializerClasses" init-param will be applied.</li>
	 * <li>{@link ConfigurableApplicationContext#refresh refresh()} will be called</li>
	 * </ul>
	 * If the context has already been refreshed or does not implement
	 * {@code ConfigurableWebApplicationContext}, none of the above will occur under the
	 * assumption that the user has performed these actions (or not) per his or her
	 * specific needs.
	 * <p>See {@link org.springframework.web.WebApplicationInitializer} for usage examples.
	 * <p>In any case, the given application context will be registered into the
	 * ServletContext under the attribute name {@link
	 * WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE} and subclasses are
	 * free to call the {@link #closeWebApplicationContext} method on container shutdown
	 * to close the application context.
	 * @param context the application context to manage
	 * @see #initWebApplicationContext(ServletContext)
	 * @see #closeWebApplicationContext(ServletContext)
	 */
	public ContextLoader(WebApplicationContext context) {
		this.context = context;
	}


	/**
	 * Specify which {@link ApplicationContextInitializer} instances should be used
	 * to initialize the application context used by this {@code ContextLoader}.
	 * @since 4.2
	 * @see #configureAndRefreshWebApplicationContext
	 * @see #customizeContext
	 */
	@SuppressWarnings("unchecked")
	public void setContextInitializers(ApplicationContextInitializer<?>... initializers) {
		if (initializers != null) {
			for (ApplicationContextInitializer<?> initializer : initializers) {
				this.contextInitializers.add((ApplicationContextInitializer<ConfigurableApplicationContext>) initializer);
			}
		}
	}


	/**
	 * 使用构造时提供的应用程序上下文初始化Spring的给定servlet上下文的Web应用程序上下文，
	 * 或者根据“{@link #CONTEXT_CLASS_PARAM contextClass}”和
	 * “{@link #CONFIG_LOCATION_PARAM contextConfigLocation}”
	 * context-params创建一个新的Web应用程序上下文
	 * @param servletContext current servlet context
	 * @return the new WebApplicationContext
	 * @see #ContextLoader(WebApplicationContext)
	 * @see #CONTEXT_CLASS_PARAM
	 * @see #CONFIG_LOCATION_PARAM
	 */
	public WebApplicationContext initWebApplicationContext(ServletContext servletContext) {
		//web.xml存在多个applicationContext配置,抛出异常
		if (servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE) != null) {
			throw new IllegalStateException(
					"Cannot initialize context because there is already a root application context present - " +
					"check whether you have multiple ContextLoader* definitions in your web.xml!");
		}
		Log logger = LogFactory.getLog(ContextLoader.class);
		servletContext.log("Initializing Spring root WebApplicationContext");
		if (logger.isInfoEnabled()) {
			logger.info("Root WebApplicationContext: initialization started");
		}
		long startTime = System.currentTimeMillis();
		try {
			// Store context in local instance variable, to guarantee that
			// it is available on ServletContext shutdown.
			//将servletContext存储在本地实例变量中，以保证它在ServletContext关闭时可用。
			if (this.context == null) {
				//初始化webApplicationContext
				this.context = createWebApplicationContext(servletContext);
			}
			// 上下文是否为ConfigurableWebApplicationContext的子类
			if (this.context instanceof ConfigurableWebApplicationContext) {
				ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) this.context;
				//如果还未激活;如果通过构造函数传递的WebApplicationContext可能isActive为true了
				if (!cwac.isActive()) {
					// 上下文尚未刷新 - >提供诸如此类的服务
					//设置父上下文，设置应用程序上下文ID等
					if (cwac.getParent() == null) {
						// The context instance was injected without an explicit parent ->
						// determine parent for root web application context, if any.
						//注入上下文实例时没有显式父 - >
						//确定根Web应用程序上下文的父级（如果有）。
						//这里我们不会用到的，所以不分析了
						ApplicationContext parent = loadParentContext(servletContext);
						cwac.setParent(parent);
					}
					configureAndRefreshWebApplicationContext(cwac, servletContext);
				}
			}
			//设置webApplicationContext属性
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this.context);

			ClassLoader ccl = Thread.currentThread().getContextClassLoader();
			//当前线程的类加载器是否为ccl
			if (ccl == ContextLoader.class.getClassLoader()) {
				currentContext = this.context;
			}
			else if (ccl != null) {
				//放到map里了
				currentContextPerThread.put(ccl, this.context);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Published root WebApplicationContext as ServletContext attribute with name [" +
						WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]");
			}
			if (logger.isInfoEnabled()) {
				long elapsedTime = System.currentTimeMillis() - startTime;
				logger.info("Root WebApplicationContext: initialization completed in " + elapsedTime + " ms");
			}
			//返回WebApplicationContext对象
			return this.context;
		}
		catch (RuntimeException ex) {
			logger.error("Context initialization failed", ex);
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ex);
			throw ex;
		}
		catch (Error err) {
			logger.error("Context initialization failed", err);
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, err);
			throw err;
		}
	}

	/**
	 * 实例化此加载器的根WebApplicationContext，或者
	 * 默认上下文类或自定义上下文类（如果已指定）。
	 * <p>此实现期望自定义上下文实现
	 * {@link ConfigurableWebApplicationContext}界面。 可以在子类中重写。
	 * <p>In addition, {@link #customizeContext} gets called prior to refreshing the
	 * context, allowing subclasses to perform custom modifications to the context.
	 * @param sc current servlet context
	 * @return the root WebApplicationContext
	 * @see ConfigurableWebApplicationContext
	 */
	protected WebApplicationContext createWebApplicationContext(ServletContext sc) {
		Class<?> contextClass = determineContextClass(sc);
		//如果class不是继承ConfigurableWebApplicationContext 类抛出个异常
		if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
			throw new ApplicationContextException("Custom context class [" + contextClass.getName() +
					"] is not of type [" + ConfigurableWebApplicationContext.class.getName() + "]");
		}
		//反射创建ConfigurableWebApplicationContext
		return (ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);
	}

	/**
	 * 返回要使用的WebApplicationContext实现类，如果未指定，则使用默认的XmlWebApplicationContext或自定义上下文类。
	 * @param servletContext current servlet context
	 * @return the WebApplicationContext implementation class to use
	 * @see #CONTEXT_CLASS_PARAM
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	protected Class<?> determineContextClass(ServletContext servletContext) {
		//获取contextClass配置WebApplicationContext
		String contextClassName = servletContext.getInitParameter(CONTEXT_CLASS_PARAM);
		//如果不为null；加载className对应的Class
		if (contextClassName != null) {
			try {
				return ClassUtils.forName(contextClassName, ClassUtils.getDefaultClassLoader());
			}
			catch (ClassNotFoundException ex) {
				throw new ApplicationContextException(
						"Failed to load custom context class [" + contextClassName + "]", ex);
			}
		}
		else {
			//从ContextLoader.properties配置文件中加载
			contextClassName = defaultStrategies.getProperty(WebApplicationContext.class.getName());
			try {
				return ClassUtils.forName(contextClassName, ContextLoader.class.getClassLoader());
			}
			catch (ClassNotFoundException ex) {
				throw new ApplicationContextException(
						"Failed to load default context class [" + contextClassName + "]", ex);
			}
		}
	}

	protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac, ServletContext sc) {
		if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
			// The application context id is still set to its original default value
			// -> assign a more useful id based on available information
			//设置ConfigurableWebApplicationContext的id
			String idParam = sc.getInitParameter(CONTEXT_ID_PARAM);
			if (idParam != null) {
				wac.setId(idParam);
			}
			else {
				// Generate default id...
				wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
						ObjectUtils.getDisplayString(sc.getContextPath()));
			}
		}
		//设置ServletContext到wac中哎嘿嘿
		wac.setServletContext(sc);
		//获取contextConfigLocation参数
		String configLocationParam = sc.getInitParameter(CONFIG_LOCATION_PARAM);
		if (configLocationParam != null) {
			wac.setConfigLocation(configLocationParam);
		}

		// The wac environment's #initPropertySources will be called in any case when the context
		// is refreshed; do it eagerly here to ensure servlet property sources are in place for
		// use in any post-processing or initialization that occurs below prior to #refresh
		//在刷新上下文时，无论如何都会调用wac环境的#initPropertySources;
		//为了用于#refresh之前发生的任何后处理或初始化在此急切地确保servlet属性源到位
		ConfigurableEnvironment env = wac.getEnvironment();
		if (env instanceof ConfigurableWebEnvironment) {
			((ConfigurableWebEnvironment) env).initPropertySources(sc, null);
		}
		customizeContext(sc, wac);
		wac.refresh();
	}

	/**
	 * 配置化该ContextLoader创建的{@link ConfigurableWebApplicationContext}，在配置位置已经提供给
	 * 上下文之后，但在上下文调用<em>refreshed<em/>之前
	 *
	 * <p>The default implementation {@linkplain #determineContextInitializerClasses(ServletContext)
	 * determines} what (if any) context initializer classes have been specified through
	 * {@linkplain #CONTEXT_INITIALIZER_CLASSES_PARAM context init parameters} and
	 * {@linkplain ApplicationContextInitializer#initialize invokes each} with the
	 * given web application context.
	 *
	 * 默认的{@linkplain #determineContextInitializerClasses(ServletContext)determines}实现;如果通过
	 * {@linkplain #CONTEXT_INITIALIZER_CLASSES_PARAM context init parameters}配置了ApplicationContextInitializer
	 * 那么将会调用每个{@linkplain ApplicationContextInitializer#initialize}使用给定的webApplicationContext
	 *
	 *
	 * <p>Any {@code ApplicationContextInitializers} implementing
	 * {@link org.springframework.core.Ordered Ordered} or marked with @{@link
	 * org.springframework.core.annotation.Order Order} will be sorted appropriately.
	 * @param sc the current servlet context
	 * @param wac the newly created application context
	 * @see #CONTEXT_INITIALIZER_CLASSES_PARAM
	 * @see ApplicationContextInitializer#initialize(ConfigurableApplicationContext)
	 */
	protected void customizeContext(ServletContext sc, ConfigurableWebApplicationContext wac) {
		//获取配置的ContextInitializerClasses类
		List<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>> initializerClasses =
				determineContextInitializerClasses(sc);
		//TODO 扩展点
		//这些class类必须是符合ApplicationContextInitializer.class的#initialize标准的才给应用
		for (Class<ApplicationContextInitializer<ConfigurableApplicationContext>> initializerClass : initializerClasses) {
			Class<?> initializerContextClass =
					GenericTypeResolver.resolveTypeArgument(initializerClass, ApplicationContextInitializer.class);
			if (initializerContextClass != null && !initializerContextClass.isInstance(wac)) {
				throw new ApplicationContextException(String.format(
						"Could not apply context initializer [%s] since its generic parameter [%s] " +
						"is not assignable from the type of application context used by this " +
						"context loader: [%s]", initializerClass.getName(), initializerContextClass.getName(),
						wac.getClass().getName()));
			}
			this.contextInitializers.add(BeanUtils.instantiateClass(initializerClass));
		}
		//排序下
		AnnotationAwareOrderComparator.sort(this.contextInitializers);
		//按照顺序挨个应用initializer到WebApplicationContext
		for (ApplicationContextInitializer<ConfigurableApplicationContext> initializer : this.contextInitializers) {
			initializer.initialize(wac);
		}
	}

	/**
	 * Return the {@link ApplicationContextInitializer} implementation classes to use
	 * if any have been specified by {@link #CONTEXT_INITIALIZER_CLASSES_PARAM}.
	 *
	 * @param servletContext current servlet context
	 * @see #CONTEXT_INITIALIZER_CLASSES_PARAM
	 */
	protected List<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>>
			determineContextInitializerClasses(ServletContext servletContext) {

		List<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>> classes =
				new ArrayList<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>>();
		//globalInitializerClasses属性
		String globalClassNames = servletContext.getInitParameter(GLOBAL_INITIALIZER_CLASSES_PARAM);
		//按照字符串切分
		if (globalClassNames != null) {
			for (String className : StringUtils.tokenizeToStringArray(globalClassNames, INIT_PARAM_DELIMITERS)) {
				classes.add(loadInitializerClass(className));
			}
		}
		//contextInitializerClasses
		String localClassNames = servletContext.getInitParameter(CONTEXT_INITIALIZER_CLASSES_PARAM);
		if (localClassNames != null) {
			for (String className : StringUtils.tokenizeToStringArray(localClassNames, INIT_PARAM_DELIMITERS)) {
				classes.add(loadInitializerClass(className));
			}
		}
		return classes;
	}

	@SuppressWarnings("unchecked")
	private Class<ApplicationContextInitializer<ConfigurableApplicationContext>> loadInitializerClass(String className) {
		try {
			Class<?> clazz = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
			//如果clazz没有继承ApplicationContextInitializer抛出异常
			if (!ApplicationContextInitializer.class.isAssignableFrom(clazz)) {
				throw new ApplicationContextException(
						"Initializer class does not implement ApplicationContextInitializer interface: " + clazz);
			}
			return (Class<ApplicationContextInitializer<ConfigurableApplicationContext>>) clazz;
		}
		catch (ClassNotFoundException ex) {
			throw new ApplicationContextException("Failed to load context initializer class [" + className + "]", ex);
		}
	}

	/**
	 * 具有默认实现的模板方法（可以由子类覆盖），以加载或获取将用作根WebApplicationContext的父上下文的ApplicationContext实例。
	 * 如果方法的返回值为null，未设置父上下文。
	 * <p>The main reason to load a parent context here is to allow multiple root
	 * web application contexts to all be children of a shared EAR context, or
	 * alternately to also share the same parent context that is visible to
	 * EJBs. For pure web applications, there is usually no need to worry about
	 * having a parent context to the root web application context.
	 * <p>The default implementation uses
	 * {@link org.springframework.context.access.ContextSingletonBeanFactoryLocator},
	 * configured via {@link #LOCATOR_FACTORY_SELECTOR_PARAM} and
	 * {@link #LOCATOR_FACTORY_KEY_PARAM}, to load a parent context
	 * which will be shared by all other users of ContextsingletonBeanFactoryLocator
	 * which also use the same configuration parameters.
	 * @param servletContext current servlet context
	 * @return the parent application context, or {@code null} if none
	 * @see org.springframework.context.access.ContextSingletonBeanFactoryLocator
	 */
	protected ApplicationContext loadParentContext(ServletContext servletContext) {
		ApplicationContext parentContext = null;
		String locatorFactorySelector = servletContext.getInitParameter(LOCATOR_FACTORY_SELECTOR_PARAM);
		String parentContextKey = servletContext.getInitParameter(LOCATOR_FACTORY_KEY_PARAM);

		if (parentContextKey != null) {
			// locatorFactorySelector may be null, indicating the default "classpath*:beanRefContext.xml"
			BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance(locatorFactorySelector);
			Log logger = LogFactory.getLog(ContextLoader.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Getting parent context definition: using parent context key of '" +
						parentContextKey + "' with BeanFactoryLocator");
			}
			this.parentContextRef = locator.useBeanFactory(parentContextKey);
			parentContext = (ApplicationContext) this.parentContextRef.getFactory();
		}

		return parentContext;
	}

	/**
	 * 关闭给定servlet上下文的Spring的Web应用程序上下文. If
	 * the default {@link #loadParentContext(ServletContext)} implementation,
	 * which uses ContextSingletonBeanFactoryLocator, has loaded any shared
	 * parent context, release one reference to that shared parent context.
	 * 如果默认的使用ContextSingletonBeanFactoryLocator的{@link #loadParentContext(ServletContext)}
	 * 加载了parent context,释放对该共享父上下文的一个引用
	 * <p>If overriding {@link #loadParentContext(ServletContext)}, you may have
	 * to override this method as well.
	 * @param servletContext the ServletContext that the WebApplicationContext runs in
	 */
	public void closeWebApplicationContext(ServletContext servletContext) {
		servletContext.log("Closing Spring root WebApplicationContext");
		try {
			//关闭ConfigurableWebApplicationContext
			if (this.context instanceof ConfigurableWebApplicationContext) {
				((ConfigurableWebApplicationContext) this.context).close();
			}
		}
		finally {
			ClassLoader ccl = Thread.currentThread().getContextClassLoader();
			if (ccl == ContextLoader.class.getClassLoader()) {
				currentContext = null;
			}
			else if (ccl != null) {
				currentContextPerThread.remove(ccl);
			}
			servletContext.removeAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
			if (this.parentContextRef != null) {
				this.parentContextRef.release();
			}
		}
	}


	/**
	 * Obtain the Spring root web application context for the current thread
	 * (i.e. for the current thread's context ClassLoader, which needs to be
	 * the web application's ClassLoader).
	 * @return the current root web application context, or {@code null}
	 * if none found
	 * @see org.springframework.web.context.support.SpringBeanAutowiringSupport
	 */
	public static WebApplicationContext getCurrentWebApplicationContext() {
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		if (ccl != null) {
			WebApplicationContext ccpt = currentContextPerThread.get(ccl);
			if (ccpt != null) {
				return ccpt;
			}
		}
		return currentContext;
	}

}

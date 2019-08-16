/*
 * Copyright 2002-2019 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.ui.context.ThemeSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.util.NestedServletException;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * HTTP请求处理程序/控制器的中央调度程序，例如 用于Web UI控制器或基于HTTP的远程服务调用。
 * 分发请求到到已注册的处理程序以处理Web请求，提供方便的映射和异常处理工具。
 * <p>
 * 该servlet非常灵活:通过适当的adapter class，它可被用于几乎任何workflow,它提供了以下区别于请求驱动的
 * web mvc框架的有用功能:
 * <ul>
 * <li>它基于JavaBeans配置机制。
 * <li>它可以通过使用任何{@link HandlerMapping}实现 - 预构建或作为应用程序的一部分提供 - 控制对处理程序对象的请求路由。
 * 默认为{@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}和
 * {@link org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping} .
 * HandlerMapping对象可以在servlet的应用程序上下文中定义为bean，实现 HandlerMapping接口，覆盖默认的HandlerMapping（如果存在）。 HandlerMappings可以被赋予任何bean名称（它们按类型进行测试）。
 *
 * <li>它可以使用任何{@link HandlerAdapter}; 这允许使用任何处理程序接口。
 * 默认适配器是{@link org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter}，
 * {@link org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter}，
 * 适用于Spring的{@link org.springframework.web.HttpRequestHandler}和
 * { @link org.springframework.web.servlet.mvc.Controller}接口。
 * 默认的{@link org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter}也将被注册。
 * HandlerAdapter对象可以作为bean添加到应用程序上下文中，覆盖默认的HandlerAdapter。
 * 与HandlerMappings一样，HandlerAdapters可以被赋予任何bean名称（它们按类型进行测试）。
 *
 * <li>可以通过{@link HandlerExceptionResolver}指定调度程序的异常解析策略，例如将某些异常映射到错误页面。
 * 默认为{@link org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerExceptionResolver}，
 * {@link org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver}和
 * {@link org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver}。
 * 可以通过应用程序上下文覆盖这些HandlerExceptionResolvers.HandlerExceptionResolver可以被赋予任何bean名称（它们按类型进行测试）。
 *
 * <li>可以通过{@link ViewResolver}实现指定其视图解析策略，将符号视图名称解析为View对象。
 * 默认为{@link org.springframework.web.servlet.view.InternalResourceViewResolver}。
 * ViewResolver对象可以作为bean添加到应用程序上下文中，覆盖默认的ViewResolver。
 * ViewResolvers可以被赋予任何bean名称（它们按类型进行测试）。
 *
 * <li>如果用户未提供{@link View}或视图名称，则配置的{@link RequestToViewNameTranslator}会将当前请求转换为视图名称。
 * 相应的bean名称是“viewNameTranslator”; 默认是
 * {@link org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator}。
 *
 * <li>解决多部分请求的策略由{@link org.springframework.web.multipart.MultipartResolver}实现确定。
 * 包括Apache Commons FileUpload和Servlet 3的实现; 典型的选择是
 * {@link org.springframework.web.multipart.commons.CommonsMultipartResolver}。
 * MultipartResolver bean名称是“multipartResolver”; 默认为none。
 *
 * <li>它的语言环境解析策略由{@link LocaleResolver}确定。开箱即用的实现通过HTTP接受标头，cookie或会话工作。
 * LocaleResolver bean名称是“localeResolver”; 默认值为{@link org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver}。
 *
 * <li>其主题解析策略由{@link ThemeResolver}决定。包括固定主题和cookie和会话存储的实现。
 * ThemeResolver bean名称是“themeResolver”; 默认为{@link org.springframework.web.servlet.theme.FixedThemeResolver}。
 * </ul>
 *
 * <p><b>注意：只有在调度程序中存在相应的{@code HandlerMapping}（用于类型级注释）和/或{@code HandlerAdapter}
 * （用于方法级注释）时，才会处理{@code @RequestMapping}注释。 </ b>默认情况下就是这种情况。
 * 但是，如果要定义自定义{@code HandlerMappings}或{@code HandlerAdapters}，则需要确保定义相应的自定义{@code DefaultAnnotationHandlerMapping}
 * 和/或{@code AnnotationMethodHandlerAdapter}
 *
 *
 *
 * <p><b>Web应用程序可以定义任意数量的DispatcherServlet.</b>
 * 每个servlet将在其自己的命名空间中运行，使用映射，处理程序等加载其自己的应用程序上下文。
 * 只有{@link org.springframework.web.context.ContextLoaderListener}加载的根应用程序上下文（如果有）将被共享。
 *
 * <p>As of Spring 3.1, {@code DispatcherServlet} may now be injected with a web
 * application context, rather than creating its own internally. This is useful in Servlet
 * 3.0+ environments, which support programmatic registration of servlet instances.
 * See the {@link #DispatcherServlet(WebApplicationContext)} javadoc for details.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Chris Beams
 * @author Rossen Stoyanchev
 * @see org.springframework.web.HttpRequestHandler
 * @see org.springframework.web.servlet.mvc.Controller
 * @see org.springframework.web.context.ContextLoaderListener
 */
@SuppressWarnings("serial")
public class DispatcherServlet extends FrameworkServlet {

    /**
     * Well-known name for the MultipartResolver object in the bean factory for this namespace.
     */
    public static final String MULTIPART_RESOLVER_BEAN_NAME = "multipartResolver";

    /**
     * Well-known name for the LocaleResolver object in the bean factory for this namespace.
     */
    public static final String LOCALE_RESOLVER_BEAN_NAME = "localeResolver";

    /**
     * Well-known name for the ThemeResolver object in the bean factory for this namespace.
     */
    public static final String THEME_RESOLVER_BEAN_NAME = "themeResolver";

    /**
     * Well-known name for the HandlerMapping object in the bean factory for this namespace.
     * Only used when "detectAllHandlerMappings" is turned off.
     *
     * @see #setDetectAllHandlerMappings
     */
    public static final String HANDLER_MAPPING_BEAN_NAME = "handlerMapping";

    /**
     * Well-known name for the HandlerAdapter object in the bean factory for this namespace.
     * Only used when "detectAllHandlerAdapters" is turned off.
     *
     * @see #setDetectAllHandlerAdapters
     */
    public static final String HANDLER_ADAPTER_BEAN_NAME = "handlerAdapter";

    /**
     * Well-known name for the HandlerExceptionResolver object in the bean factory for this namespace.
     * Only used when "detectAllHandlerExceptionResolvers" is turned off.
     *
     * @see #setDetectAllHandlerExceptionResolvers
     */
    public static final String HANDLER_EXCEPTION_RESOLVER_BEAN_NAME = "handlerExceptionResolver";

    /**
     * Well-known name for the RequestToViewNameTranslator object in the bean factory for this namespace.
     */
    public static final String REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME = "viewNameTranslator";

    /**
     * Well-known name for the ViewResolver object in the bean factory for this namespace.
     * Only used when "detectAllViewResolvers" is turned off.
     *
     * @see #setDetectAllViewResolvers
     */
    public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";

    /**
     * Well-known name for the FlashMapManager object in the bean factory for this namespace.
     */
    public static final String FLASH_MAP_MANAGER_BEAN_NAME = "flashMapManager";

    /**
     * Request attribute to hold the current web application context.
     * Otherwise only the global web app context is obtainable by tags etc.
     *
     * @see org.springframework.web.servlet.support.RequestContextUtils#findWebApplicationContext
     */
    public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.class.getName() + ".CONTEXT";

    /**
     * Request attribute to hold the current LocaleResolver, retrievable by views.
     *
     * @see org.springframework.web.servlet.support.RequestContextUtils#getLocaleResolver
     */
    public static final String LOCALE_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".LOCALE_RESOLVER";

    /**
     * Request attribute to hold the current ThemeResolver, retrievable by views.
     *
     * @see org.springframework.web.servlet.support.RequestContextUtils#getThemeResolver
     */
    public static final String THEME_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_RESOLVER";

    /**
     * Request attribute to hold the current ThemeSource, retrievable by views.
     *
     * @see org.springframework.web.servlet.support.RequestContextUtils#getThemeSource
     */
    public static final String THEME_SOURCE_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_SOURCE";

    /**
     * Name of request attribute that holds a read-only {@code Map<String,?>}
     * with "input" flash attributes saved by a previous request, if any.
     *
     * @see org.springframework.web.servlet.support.RequestContextUtils#getInputFlashMap(HttpServletRequest)
     */
    public static final String INPUT_FLASH_MAP_ATTRIBUTE = DispatcherServlet.class.getName() + ".INPUT_FLASH_MAP";

    /**
     * Name of request attribute that holds the "output" {@link FlashMap} with
     * attributes to save for a subsequent request.
     *
     * @see org.springframework.web.servlet.support.RequestContextUtils#getOutputFlashMap(HttpServletRequest)
     */
    public static final String OUTPUT_FLASH_MAP_ATTRIBUTE = DispatcherServlet.class.getName() + ".OUTPUT_FLASH_MAP";

    /**
     * Name of request attribute that holds the {@link FlashMapManager}.
     *
     * @see org.springframework.web.servlet.support.RequestContextUtils#getFlashMapManager(HttpServletRequest)
     */
    public static final String FLASH_MAP_MANAGER_ATTRIBUTE = DispatcherServlet.class.getName() + ".FLASH_MAP_MANAGER";

    /**
     * Name of request attribute that exposes an Exception resolved with an
     * {@link HandlerExceptionResolver} but where no view was rendered
     * (e.g. setting the status code).
     */
    public static final String EXCEPTION_ATTRIBUTE = DispatcherServlet.class.getName() + ".EXCEPTION";

    /**
     * Log category to use when no mapped handler is found for a request.
     */
    public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

    /**
     * Name of the class path resource (relative to the DispatcherServlet class)
     * that defines DispatcherServlet's default strategy names.
     */
    private static final String DEFAULT_STRATEGIES_PATH = "DispatcherServlet.properties";

    /**
     * Common prefix that DispatcherServlet's default strategy attributes start with.
     */
    private static final String DEFAULT_STRATEGIES_PREFIX = "org.springframework.web.servlet";

    /**
     * Additional logger to use when no mapped handler is found for a request.
     */
    protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);

    private static final Properties defaultStrategies;

    static {
        // Load default strategy implementations from properties file.
        // This is currently strictly internal and not meant to be customized
        // by application developers.
        try {
            ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, DispatcherServlet.class);
            defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load '" + DEFAULT_STRATEGIES_PATH + "': " + ex.getMessage());
        }
    }

    /** Detect all HandlerMappings or just expect "handlerMapping" bean? */
    /**
     * 检测所有HandlerMappings或只是期望“handlerMapping”bean？
     */
    private boolean detectAllHandlerMappings = true;

    /**
     * Detect all HandlerAdapters or just expect "handlerAdapter" bean?
     */
    private boolean detectAllHandlerAdapters = true;

    /**
     * Detect all HandlerExceptionResolvers or just expect "handlerExceptionResolver" bean?
     */
    private boolean detectAllHandlerExceptionResolvers = true;

    /**
     * Detect all ViewResolvers or just expect "viewResolver" bean?
     */
    private boolean detectAllViewResolvers = true;

    /**
     * Throw a NoHandlerFoundException if no Handler was found to process this request?
     **/
    private boolean throwExceptionIfNoHandlerFound = false;

    /**
     * Perform cleanup of request attributes after include request?
     */
    private boolean cleanupAfterInclude = true;

    /**
     * MultipartResolver used by this servlet
     */
    private MultipartResolver multipartResolver;

    /**
     * LocaleResolver used by this servlet
     */
    private LocaleResolver localeResolver;

    /**
     * ThemeResolver used by this servlet
     */
    private ThemeResolver themeResolver;

    /**
     * List of HandlerMappings used by this servlet
     */
    private List<HandlerMapping> handlerMappings;

    /**
     * List of HandlerAdapters used by this servlet
     */
    private List<HandlerAdapter> handlerAdapters;

    /**
     * List of HandlerExceptionResolvers used by this servlet
     */
    private List<HandlerExceptionResolver> handlerExceptionResolvers;

    /**
     * RequestToViewNameTranslator used by this servlet
     */
    private RequestToViewNameTranslator viewNameTranslator;

    /**
     * FlashMapManager used by this servlet
     */
    private FlashMapManager flashMapManager;

    /**
     * List of ViewResolvers used by this servlet
     */
    private List<ViewResolver> viewResolvers;


    /**
     * Create a new {@code DispatcherServlet} that will create its own internal web
     * application context based on defaults and values provided through servlet
     * init-params. Typically used in Servlet 2.5 or earlier environments, where the only
     * option for servlet registration is through {@code web.xml} which requires the use
     * of a no-arg constructor.
     * <p>Calling {@link #setContextConfigLocation} (init-param 'contextConfigLocation')
     * will dictate which XML files will be loaded by the
     * {@linkplain #DEFAULT_CONTEXT_CLASS default XmlWebApplicationContext}
     * <p>Calling {@link #setContextClass} (init-param 'contextClass') overrides the
     * default {@code XmlWebApplicationContext} and allows for specifying an alternative class,
     * such as {@code AnnotationConfigWebApplicationContext}.
     * <p>Calling {@link #setContextInitializerClasses} (init-param 'contextInitializerClasses')
     * indicates which {@code ApplicationContextInitializer} classes should be used to
     * further configure the internal application context prior to refresh().
     *
     * @see #DispatcherServlet(WebApplicationContext)
     */
    public DispatcherServlet() {
        super();
        setDispatchOptionsRequest(true);
    }

    /**
     * Create a new {@code DispatcherServlet} with the given web application context. This
     * constructor is useful in Servlet 3.0+ environments where instance-based registration
     * of servlets is possible through the {@link ServletContext#addServlet} API.
     * <p>Using this constructor indicates that the following properties / init-params
     * will be ignored:
     * <ul>
     * <li>{@link #setContextClass(Class)} / 'contextClass'</li>
     * <li>{@link #setContextConfigLocation(String)} / 'contextConfigLocation'</li>
     * <li>{@link #setContextAttribute(String)} / 'contextAttribute'</li>
     * <li>{@link #setNamespace(String)} / 'namespace'</li>
     * </ul>
     * <p>The given web application context may or may not yet be {@linkplain
     * ConfigurableApplicationContext#refresh() refreshed}. If it has <strong>not</strong>
     * already been refreshed (the recommended approach), then the following will occur:
     * <ul>
     * <li>If the given context does not already have a {@linkplain
     * ConfigurableApplicationContext#setParent parent}, the root application context
     * will be set as the parent.</li>
     * <li>If the given context has not already been assigned an {@linkplain
     * ConfigurableApplicationContext#setId id}, one will be assigned to it</li>
     * <li>{@code ServletContext} and {@code ServletConfig} objects will be delegated to
     * the application context</li>
     * <li>{@link #postProcessWebApplicationContext} will be called</li>
     * <li>Any {@code ApplicationContextInitializer}s specified through the
     * "contextInitializerClasses" init-param or through the {@link
     * #setContextInitializers} property will be applied.</li>
     * <li>{@link ConfigurableApplicationContext#refresh refresh()} will be called if the
     * context implements {@link ConfigurableApplicationContext}</li>
     * </ul>
     * If the context has already been refreshed, none of the above will occur, under the
     * assumption that the user has performed these actions (or not) per their specific
     * needs.
     * <p>See {@link org.springframework.web.WebApplicationInitializer} for usage examples.
     *
     * @param webApplicationContext the context to use
     * @see #initWebApplicationContext
     * @see #configureAndRefreshWebApplicationContext
     * @see org.springframework.web.WebApplicationInitializer
     */
    public DispatcherServlet(WebApplicationContext webApplicationContext) {
        super(webApplicationContext);
        setDispatchOptionsRequest(true);
    }


    /**
     * Set whether to detect all HandlerMapping beans in this servlet's context. Otherwise,
     * just a single bean with name "handlerMapping" will be expected.
     * <p>Default is "true". Turn this off if you want this servlet to use a single
     * HandlerMapping, despite multiple HandlerMapping beans being defined in the context.
     */
    public void setDetectAllHandlerMappings(boolean detectAllHandlerMappings) {
        this.detectAllHandlerMappings = detectAllHandlerMappings;
    }

    /**
     * Set whether to detect all HandlerAdapter beans in this servlet's context. Otherwise,
     * just a single bean with name "handlerAdapter" will be expected.
     * <p>Default is "true". Turn this off if you want this servlet to use a single
     * HandlerAdapter, despite multiple HandlerAdapter beans being defined in the context.
     */
    public void setDetectAllHandlerAdapters(boolean detectAllHandlerAdapters) {
        this.detectAllHandlerAdapters = detectAllHandlerAdapters;
    }

    /**
     * Set whether to detect all HandlerExceptionResolver beans in this servlet's context. Otherwise,
     * just a single bean with name "handlerExceptionResolver" will be expected.
     * <p>Default is "true". Turn this off if you want this servlet to use a single
     * HandlerExceptionResolver, despite multiple HandlerExceptionResolver beans being defined in the context.
     */
    public void setDetectAllHandlerExceptionResolvers(boolean detectAllHandlerExceptionResolvers) {
        this.detectAllHandlerExceptionResolvers = detectAllHandlerExceptionResolvers;
    }

    /**
     * Set whether to detect all ViewResolver beans in this servlet's context. Otherwise,
     * just a single bean with name "viewResolver" will be expected.
     * <p>Default is "true". Turn this off if you want this servlet to use a single
     * ViewResolver, despite multiple ViewResolver beans being defined in the context.
     */
    public void setDetectAllViewResolvers(boolean detectAllViewResolvers) {
        this.detectAllViewResolvers = detectAllViewResolvers;
    }

    /**
     * Set whether to throw a NoHandlerFoundException when no Handler was found for this request.
     * This exception can then be caught with a HandlerExceptionResolver or an
     * {@code @ExceptionHandler} controller method.
     * <p>Note that if {@link org.springframework.web.servlet.resource.DefaultServletHttpRequestHandler}
     * is used, then requests will always be forwarded to the default servlet and a
     * NoHandlerFoundException would never be thrown in that case.
     * <p>Default is "false", meaning the DispatcherServlet sends a NOT_FOUND error through the
     * Servlet response.
     *
     * @since 4.0
     */
    public void setThrowExceptionIfNoHandlerFound(boolean throwExceptionIfNoHandlerFound) {
        this.throwExceptionIfNoHandlerFound = throwExceptionIfNoHandlerFound;
    }

    /**
     * Set whether to perform cleanup of request attributes after an include request, that is,
     * whether to reset the original state of all request attributes after the DispatcherServlet
     * has processed within an include request. Otherwise, just the DispatcherServlet's own
     * request attributes will be reset, but not model attributes for JSPs or special attributes
     * set by views (for example, JSTL's).
     * <p>Default is "true", which is strongly recommended. Views should not rely on request attributes
     * having been set by (dynamic) includes. This allows JSP views rendered by an included controller
     * to use any model attributes, even with the same names as in the main JSP, without causing side
     * effects. Only turn this off for special needs, for example to deliberately allow main JSPs to
     * access attributes from JSP views rendered by an included controller.
     */
    public void setCleanupAfterInclude(boolean cleanupAfterInclude) {
        this.cleanupAfterInclude = cleanupAfterInclude;
    }


    /**
     * 此实现调用{@link #initStrategies}
     */
    @Override
    protected void onRefresh(ApplicationContext context) {
        initStrategies(context);
    }

    /**
     * 初始化此servlet使用的策略对象。
     * <p>可以在子类中重写以初始化其他策略对象。
     */
    protected void initStrategies(ApplicationContext context) {
        //用于解析文件上传
        initMultipartResolver(context);
        //用于配置国际化
        initLocaleResolver(context);
        //用于控制主题页面的风格
        initThemeResolver(context);
        //用于为一个请求找到处理类或者处理方法
        initHandlerMappings(context);
        //适配请求处理对象，调用处理器的方法处理请求
        initHandlerAdapters(context);
        //异常解析处理器，比如给异常返回状态码、返回json文件、返回到某个错误页面
        initHandlerExceptionResolvers(context);
        //在返回view为空时，它能够根据request请求获得viewName
        initRequestToViewNameTranslator(context);
        //视图解析器，把逻辑视图名称解析为View对象
        initViewResolvers(context);
        //重定向请求传参用获取上一个request的参数信息
        initFlashMapManager(context);
    }

    /**
     * 初始化此类使用的MultipartResolver
     * <p>如果没有在BeanFactory中为此命名空间定义具有给定名称的bean，则不提供Multipart的处理。
     */
    private void initMultipartResolver(ApplicationContext context) {
        try {
            this.multipartResolver = context.getBean(MULTIPART_RESOLVER_BEAN_NAME, MultipartResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Using MultipartResolver [" + this.multipartResolver + "]");
            }
        } catch (NoSuchBeanDefinitionException ex) {
            // Default is no multipart resolver.
            this.multipartResolver = null;
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate MultipartResolver with name '" + MULTIPART_RESOLVER_BEAN_NAME +
                        "': no multipart request handling provided");
            }
        }
    }

    /**
     * 初始化此类使用的LocaleResolver。
     * <p>如果在此命名空间的BeanFactory中没有使用给定名称定义bean，则默认为AcceptHeaderLocaleResolver。
     */
    private void initLocaleResolver(ApplicationContext context) {
        try {
            this.localeResolver = context.getBean(LOCALE_RESOLVER_BEAN_NAME, LocaleResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Using LocaleResolver [" + this.localeResolver + "]");
            }
        } catch (NoSuchBeanDefinitionException ex) {
            // We need to use the default.
            // 创建默认的哎嘿嘿
            this.localeResolver = getDefaultStrategy(context, LocaleResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate LocaleResolver with name '" + LOCALE_RESOLVER_BEAN_NAME +
                        "': using default [" + this.localeResolver + "]");
            }
        }
    }

    /**
     * 初始化此类使用的ThemeResolver。
     * <p>如果在此命名空间的BeanFactory中没有使用给定名称定义bean，则默认为FixedThemeResolver。
     */
    private void initThemeResolver(ApplicationContext context) {
        try {
            this.themeResolver = context.getBean(THEME_RESOLVER_BEAN_NAME, ThemeResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Using ThemeResolver [" + this.themeResolver + "]");
            }
        } catch (NoSuchBeanDefinitionException ex) {
            // 从
            this.themeResolver = getDefaultStrategy(context, ThemeResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate ThemeResolver with name '" + THEME_RESOLVER_BEAN_NAME +
                        "': using default [" + this.themeResolver + "]");
            }
        }
    }

    /**
     * 初始化此类使用的HandlerMappings。
     * <p>如果没有在BeanFactory中为此命名空间定义HandlerMapping bean，我们默认为BeanNameUrlHandlerMapping。
     */
    private void initHandlerMappings(ApplicationContext context) {
        this.handlerMappings = null;

        if (this.detectAllHandlerMappings) {
            // Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerMapping> matchingBeans =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerMappings = new ArrayList<HandlerMapping>(matchingBeans.values());
                // We keep HandlerMappings in sorted order.
                AnnotationAwareOrderComparator.sort(this.handlerMappings);
            }
        } else {
            try {
                HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
                this.handlerMappings = Collections.singletonList(hm);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default HandlerMapping later.
            }
        }

        // Ensure we have at least one HandlerMapping, by registering
        // a default HandlerMapping if no other mappings are found.
        if (this.handlerMappings == null) {
            this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerMappings found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    /**
     * Initialize the HandlerAdapters used by this class.
     * <p>If no HandlerAdapter beans are defined in the BeanFactory for this namespace,
     * we default to SimpleControllerHandlerAdapter.
     */
    private void initHandlerAdapters(ApplicationContext context) {
        this.handlerAdapters = null;

        if (this.detectAllHandlerAdapters) {
            // Find all HandlerAdapters in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerAdapter> matchingBeans =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerAdapters = new ArrayList<HandlerAdapter>(matchingBeans.values());
                // We keep HandlerAdapters in sorted order.
                AnnotationAwareOrderComparator.sort(this.handlerAdapters);
            }
        } else {
            try {
                HandlerAdapter ha = context.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
                this.handlerAdapters = Collections.singletonList(ha);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default HandlerAdapter later.
            }
        }

        // Ensure we have at least some HandlerAdapters, by registering
        // default HandlerAdapters if no other adapters are found.
        if (this.handlerAdapters == null) {
            this.handlerAdapters = getDefaultStrategies(context, HandlerAdapter.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerAdapters found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    /**
     * 初始化此类使用的HandlerExceptionResolver。
     * <p>如果在此命名空间的BeanFactory中没有使用给定名称定义bean，则默认情况下不会出现异常解析程序。
     */
    private void initHandlerExceptionResolvers(ApplicationContext context) {
        this.handlerExceptionResolvers = null;

        if (this.detectAllHandlerExceptionResolvers) {
            // Find all HandlerExceptionResolvers in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerExceptionResolver> matchingBeans = BeanFactoryUtils
                    .beansOfTypeIncludingAncestors(context, HandlerExceptionResolver.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerExceptionResolvers = new ArrayList<HandlerExceptionResolver>(matchingBeans.values());
                // We keep HandlerExceptionResolvers in sorted order.
                AnnotationAwareOrderComparator.sort(this.handlerExceptionResolvers);
            }
        } else {
            try {
                HandlerExceptionResolver her =
                        context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, HandlerExceptionResolver.class);
                this.handlerExceptionResolvers = Collections.singletonList(her);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, no HandlerExceptionResolver is fine too.
            }
        }

        // Ensure we have at least some HandlerExceptionResolvers, by registering
        // default HandlerExceptionResolvers if no other resolvers are found.
        if (this.handlerExceptionResolvers == null) {
            this.handlerExceptionResolvers = getDefaultStrategies(context, HandlerExceptionResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerExceptionResolvers found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    /**
     * 初始化此servlet实例使用的RequestToViewNameTranslator。
     * <p>如果未配置任何实现，则默认为DefaultRequestToViewNameTranslator。
     */
    private void initRequestToViewNameTranslator(ApplicationContext context) {
        try {
            this.viewNameTranslator =
                    context.getBean(REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME, RequestToViewNameTranslator.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Using RequestToViewNameTranslator [" + this.viewNameTranslator + "]");
            }
        } catch (NoSuchBeanDefinitionException ex) {
            // We need to use the default.
            this.viewNameTranslator = getDefaultStrategy(context, RequestToViewNameTranslator.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate RequestToViewNameTranslator with name '" +
                        REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME + "': using default [" + this.viewNameTranslator +
                        "]");
            }
        }
    }

    /**
     * 初始化此类使用的ViewResolvers。
     * <p>如果没有为此命名空间的BeanFactory定义ViewResolver bean，我们默认为InternalResourceViewResolver。
     */
    private void initViewResolvers(ApplicationContext context) {
        this.viewResolvers = null;

        if (this.detectAllViewResolvers) {
            // Find all ViewResolvers in the ApplicationContext, including ancestor contexts.
            Map<String, ViewResolver> matchingBeans =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(context, ViewResolver.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.viewResolvers = new ArrayList<ViewResolver>(matchingBeans.values());
                // We keep ViewResolvers in sorted order.
                AnnotationAwareOrderComparator.sort(this.viewResolvers);
            }
        } else {
            try {
                ViewResolver vr = context.getBean(VIEW_RESOLVER_BEAN_NAME, ViewResolver.class);
                this.viewResolvers = Collections.singletonList(vr);
            } catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default ViewResolver later.
            }
        }

        // Ensure we have at least one ViewResolver, by registering
        // a default ViewResolver if no other resolvers are found.
        if (this.viewResolvers == null) {
            this.viewResolvers = getDefaultStrategies(context, ViewResolver.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No ViewResolvers found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    /**
     * 初始化此servlet实例使用的{@link FlashMapManager}。
     * <p>如果没有配置任何实现，那么我们默认为{@code org.springframework.web.servlet.support.SessionFlashMapManager}。
     */
    private void initFlashMapManager(ApplicationContext context) {
        try {
            this.flashMapManager = context.getBean(FLASH_MAP_MANAGER_BEAN_NAME, FlashMapManager.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Using FlashMapManager [" + this.flashMapManager + "]");
            }
        } catch (NoSuchBeanDefinitionException ex) {
            // We need to use the default.
            this.flashMapManager = getDefaultStrategy(context, FlashMapManager.class);
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate FlashMapManager with name '" +
                        FLASH_MAP_MANAGER_BEAN_NAME + "': using default [" + this.flashMapManager + "]");
            }
        }
    }

    /**
     * Return this servlet's ThemeSource, if any; else return {@code null}.
     * <p>Default is to return the WebApplicationContext as ThemeSource,
     * provided that it implements the ThemeSource interface.
     *
     * @return the ThemeSource, if any
     * @see #getWebApplicationContext()
     */
    public final ThemeSource getThemeSource() {
        if (getWebApplicationContext() instanceof ThemeSource) {
            return (ThemeSource) getWebApplicationContext();
        } else {
            return null;
        }
    }

    /**
     * Obtain this servlet's MultipartResolver, if any.
     *
     * @return the MultipartResolver used by this servlet, or {@code null} if none
     * (indicating that no multipart support is available)
     */
    public final MultipartResolver getMultipartResolver() {
        return this.multipartResolver;
    }

    /**
     * 返回给定策略接口的默认策略对象。
     * <p>默认实现委托给{@link #getDefaultStrategies}，期望列表中有一个对象。
     *
     * @param context           the current WebApplicationContext
     * @param strategyInterface the strategy interface
     * @return the corresponding strategy object
     * @see #getDefaultStrategies
     */
    protected <T> T getDefaultStrategy(ApplicationContext context, Class<T> strategyInterface) {
        List<T> strategies = getDefaultStrategies(context, strategyInterface);
        if (strategies.size() != 1) {
            throw new BeanInitializationException(
                    "DispatcherServlet needs exactly 1 strategy for interface [" + strategyInterface.getName() + "]");
        }
        return strategies.get(0);
    }

    /**
     * Create a List of default strategy objects for the given strategy interface.
     * <p>The default implementation uses the "DispatcherServlet.properties" file (in the same
     * package as the DispatcherServlet class) to determine the class names. It instantiates
     * the strategy objects through the context's BeanFactory.
     *
     * @param context           the current WebApplicationContext
     * @param strategyInterface the strategy interface
     * @return the List of corresponding strategy objects
     */
    @SuppressWarnings("unchecked")
    protected <T> List<T> getDefaultStrategies(ApplicationContext context, Class<T> strategyInterface) {
        //class 名称
        String key = strategyInterface.getName();
        //从DispatcherServlet.properties配置文件寻找className对应的类
        String value = defaultStrategies.getProperty(key);
        if (value != null) {
            //按逗号切分
            String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
            List<T> strategies = new ArrayList<T>(classNames.length);
            for (String className : classNames) {
                try {
                    //加载Class对象
                    Class<?> clazz = ClassUtils.forName(className, DispatcherServlet.class.getClassLoader());
                    //创建对象
                    Object strategy = createDefaultStrategy(context, clazz);
                    strategies.add((T) strategy);
                } catch (ClassNotFoundException ex) {
                    throw new BeanInitializationException(
                            "Could not find DispatcherServlet's default strategy class [" + className +
                                    "] for interface [" + key + "]", ex);
                } catch (LinkageError err) {
                    throw new BeanInitializationException(
                            "Error loading DispatcherServlet's default strategy class [" + className +
                                    "] for interface [" + key + "]: problem with class file or dependent class", err);
                }
            }
            return strategies;
        } else {
            return new LinkedList<T>();
        }
    }

    /**
     * Create a default strategy.
     * <p>The default implementation uses
     * {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory#createBean}.
     *
     * @param context the current WebApplicationContext
     * @param clazz   the strategy implementation class to instantiate
     * @return the fully configured strategy instance
     * @see org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()
     * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#createBean
     */
    protected Object createDefaultStrategy(ApplicationContext context, Class<?> clazz) {
        return context.getAutowireCapableBeanFactory().createBean(clazz);
    }


    /**
     * Exposes the DispatcherServlet-specific request attributes and delegates to {@link #doDispatch}
     * for the actual dispatching.
     */
    @Override
    protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (logger.isDebugEnabled()) {
            String resumed = WebAsyncUtils.getAsyncManager(request).hasConcurrentResult() ? " resumed" : "";
            logger.debug("DispatcherServlet with name '" + getServletName() + "'" + resumed +
                    " processing " + request.getMethod() + " request for [" + getRequestUri(request) + "]");
        }

        // Keep a snapshot of the request attributes in case of an include,
        // to be able to restore the original attributes after the include.
        Map<String, Object> attributesSnapshot = null;
        if (WebUtils.isIncludeRequest(request)) {
            attributesSnapshot = new HashMap<String, Object>();
            Enumeration<?> attrNames = request.getAttributeNames();
            while (attrNames.hasMoreElements()) {
                String attrName = (String) attrNames.nextElement();
                if (this.cleanupAfterInclude || attrName.startsWith(DEFAULT_STRATEGIES_PREFIX)) {
                    attributesSnapshot.put(attrName, request.getAttribute(attrName));
                }
            }
        }

        // Make framework objects available to handlers and view objects.
        request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());
        request.setAttribute(LOCALE_RESOLVER_ATTRIBUTE, this.localeResolver);
        request.setAttribute(THEME_RESOLVER_ATTRIBUTE, this.themeResolver);
        request.setAttribute(THEME_SOURCE_ATTRIBUTE, getThemeSource());

        FlashMap inputFlashMap = this.flashMapManager.retrieveAndUpdate(request, response);
        if (inputFlashMap != null) {
            request.setAttribute(INPUT_FLASH_MAP_ATTRIBUTE, Collections.unmodifiableMap(inputFlashMap));
        }
        request.setAttribute(OUTPUT_FLASH_MAP_ATTRIBUTE, new FlashMap());
        request.setAttribute(FLASH_MAP_MANAGER_ATTRIBUTE, this.flashMapManager);

        try {
            doDispatch(request, response);
        } finally {
            if (!WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
                // Restore the original attribute snapshot, in case of an include.
                if (attributesSnapshot != null) {
                    restoreAttributesAfterInclude(request, attributesSnapshot);
                }
            }
        }
    }

    /**
     * Process the actual dispatching to the handler.
     * <p>The handler will be obtained by applying the servlet's HandlerMappings in order.
     * The HandlerAdapter will be obtained by querying the servlet's installed HandlerAdapters
     * to find the first that supports the handler class.
     * <p>All HTTP methods are handled by this method. It's up to HandlerAdapters or handlers
     * themselves to decide which methods are acceptable.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @throws Exception in case of any kind of processing failure
     */
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpServletRequest processedRequest = request;
        HandlerExecutionChain mappedHandler = null;
        boolean multipartRequestParsed = false;

        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

        try {
            ModelAndView mv = null;
            Exception dispatchException = null;

            try {
                processedRequest = checkMultipart(request);
                multipartRequestParsed = (processedRequest != request);

                // Determine handler for the current request.
                mappedHandler = getHandler(processedRequest);
                if (mappedHandler == null || mappedHandler.getHandler() == null) {
                    noHandlerFound(processedRequest, response);
                    return;
                }

                // Determine handler adapter for the current request.
                HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

                // Process last-modified header, if supported by the handler.
                String method = request.getMethod();
                boolean isGet = "GET".equals(method);
                if (isGet || "HEAD".equals(method)) {
                    long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
                    if (logger.isDebugEnabled()) {
                        logger.debug("Last-Modified value for [" + getRequestUri(request) + "] is: " + lastModified);
                    }
                    if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
                        return;
                    }
                }

                if (!mappedHandler.applyPreHandle(processedRequest, response)) {
                    return;
                }

                // Actually invoke the handler.
                mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

                if (asyncManager.isConcurrentHandlingStarted()) {
                    return;
                }

                applyDefaultViewName(processedRequest, mv);
                mappedHandler.applyPostHandle(processedRequest, response, mv);
            } catch (Exception ex) {
                dispatchException = ex;
            } catch (Throwable err) {
                // As of 4.3, we're processing Errors thrown from handler methods as well,
                // making them available for @ExceptionHandler methods and other scenarios.
                dispatchException = new NestedServletException("Handler dispatch failed", err);
            }
            processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
        } catch (Exception ex) {
            triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
        } catch (Throwable err) {
            triggerAfterCompletion(processedRequest, response, mappedHandler,
                    new NestedServletException("Handler processing failed", err));
        } finally {
            if (asyncManager.isConcurrentHandlingStarted()) {
                // Instead of postHandle and afterCompletion
                if (mappedHandler != null) {
                    mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
                }
            } else {
                // Clean up any resources used by a multipart request.
                if (multipartRequestParsed) {
                    cleanupMultipart(processedRequest);
                }
            }
        }
    }

    /**
     * Do we need view name translation?
     */
    private void applyDefaultViewName(HttpServletRequest request, ModelAndView mv) throws Exception {
        if (mv != null && !mv.hasView()) {
            mv.setViewName(getDefaultViewName(request));
        }
    }

    /**
     * Handle the result of handler selection and handler invocation, which is
     * either a ModelAndView or an Exception to be resolved to a ModelAndView.
     */
    private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
                                       HandlerExecutionChain mappedHandler, ModelAndView mv, Exception exception) throws Exception {

        boolean errorView = false;

        if (exception != null) {
            if (exception instanceof ModelAndViewDefiningException) {
                logger.debug("ModelAndViewDefiningException encountered", exception);
                mv = ((ModelAndViewDefiningException) exception).getModelAndView();
            } else {
                Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
                mv = processHandlerException(request, response, handler, exception);
                errorView = (mv != null);
            }
        }

        // Did the handler return a view to render?
        if (mv != null && !mv.wasCleared()) {
            render(mv, request, response);
            if (errorView) {
                WebUtils.clearErrorRequestAttributes(request);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Null ModelAndView returned to DispatcherServlet with name '" + getServletName() +
                        "': assuming HandlerAdapter completed request handling");
            }
        }

        if (WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
            // Concurrent handling started during a forward
            return;
        }

        if (mappedHandler != null) {
            mappedHandler.triggerAfterCompletion(request, response, null);
        }
    }

    /**
     * Build a LocaleContext for the given request, exposing the request's primary locale as current locale.
     * <p>The default implementation uses the dispatcher's LocaleResolver to obtain the current locale,
     * which might change during a request.
     *
     * @param request current HTTP request
     * @return the corresponding LocaleContext
     */
    @Override
    protected LocaleContext buildLocaleContext(final HttpServletRequest request) {
        if (this.localeResolver instanceof LocaleContextResolver) {
            return ((LocaleContextResolver) this.localeResolver).resolveLocaleContext(request);
        } else {
            return new LocaleContext() {
                @Override
                public Locale getLocale() {
                    return localeResolver.resolveLocale(request);
                }
            };
        }
    }

    /**
     * Convert the request into a multipart request, and make multipart resolver available.
     * <p>If no multipart resolver is set, simply use the existing request.
     *
     * @param request current HTTP request
     * @return the processed request (multipart wrapper if necessary)
     * @see MultipartResolver#resolveMultipart
     */
    protected HttpServletRequest checkMultipart(HttpServletRequest request) throws MultipartException {
        if (this.multipartResolver != null && this.multipartResolver.isMultipart(request)) {
            if (WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class) != null) {
                logger.debug("Request is already a MultipartHttpServletRequest - if not in a forward, " +
                        "this typically results from an additional MultipartFilter in web.xml");
            } else if (hasMultipartException(request)) {
                logger.debug("Multipart resolution previously failed for current request - " +
                        "skipping re-resolution for undisturbed error rendering");
            } else {
                try {
                    return this.multipartResolver.resolveMultipart(request);
                } catch (MultipartException ex) {
                    if (request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE) != null) {
                        logger.debug("Multipart resolution failed for error dispatch", ex);
                        // Keep processing error dispatch with regular request handle below
                    } else {
                        throw ex;
                    }
                }
            }
        }
        // If not returned before: return original request.
        return request;
    }

    /**
     * Check "javax.servlet.error.exception" attribute for a multipart exception.
     */
    private boolean hasMultipartException(HttpServletRequest request) {
        Throwable error = (Throwable) request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);
        while (error != null) {
            if (error instanceof MultipartException) {
                return true;
            }
            error = error.getCause();
        }
        return false;
    }

    /**
     * Clean up any resources used by the given multipart request (if any).
     *
     * @param request current HTTP request
     * @see MultipartResolver#cleanupMultipart
     */
    protected void cleanupMultipart(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest =
                WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class);
        if (multipartRequest != null) {
            this.multipartResolver.cleanupMultipart(multipartRequest);
        }
    }

    /**
     * Return the HandlerExecutionChain for this request.
     * <p>Tries all handler mappings in order.
     *
     * @param request current HTTP request
     * @return the HandlerExecutionChain, or {@code null} if no handler could be found
     */
    protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        for (HandlerMapping hm : this.handlerMappings) {
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "Testing handler map [" + hm + "] in DispatcherServlet with name '" + getServletName() + "'");
            }
            HandlerExecutionChain handler = hm.getHandler(request);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

    /**
     * No handler found -> set appropriate HTTP response status.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @throws Exception if preparing the response failed
     */
    protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (pageNotFoundLogger.isWarnEnabled()) {
            pageNotFoundLogger.warn("No mapping found for HTTP request with URI [" + getRequestUri(request) +
                    "] in DispatcherServlet with name '" + getServletName() + "'");
        }
        if (this.throwExceptionIfNoHandlerFound) {
            throw new NoHandlerFoundException(request.getMethod(), getRequestUri(request),
                    new ServletServerHttpRequest(request).getHeaders());
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Return the HandlerAdapter for this handler object.
     *
     * @param handler the handler object to find an adapter for
     * @throws ServletException if no HandlerAdapter can be found for the handler. This is a fatal error.
     */
    protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
        for (HandlerAdapter ha : this.handlerAdapters) {
            if (logger.isTraceEnabled()) {
                logger.trace("Testing handler adapter [" + ha + "]");
            }
            if (ha.supports(handler)) {
                return ha;
            }
        }
        throw new ServletException("No adapter for handler [" + handler +
                "]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
    }

    /**
     * Determine an error ModelAndView via the registered HandlerExceptionResolvers.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  the executed handler, or {@code null} if none chosen at the time of the exception
     *                 (for example, if multipart resolution failed)
     * @param ex       the exception that got thrown during handler execution
     * @return a corresponding ModelAndView to forward to
     * @throws Exception if no error ModelAndView found
     */
    protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response,
                                                   Object handler, Exception ex) throws Exception {

        // Check registered HandlerExceptionResolvers...
        ModelAndView exMv = null;
        for (HandlerExceptionResolver handlerExceptionResolver : this.handlerExceptionResolvers) {
            exMv = handlerExceptionResolver.resolveException(request, response, handler, ex);
            if (exMv != null) {
                break;
            }
        }
        if (exMv != null) {
            if (exMv.isEmpty()) {
                request.setAttribute(EXCEPTION_ATTRIBUTE, ex);
                return null;
            }
            // We might still need view name translation for a plain error model...
            if (!exMv.hasView()) {
                exMv.setViewName(getDefaultViewName(request));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Handler execution resulted in exception - forwarding to resolved error view: " + exMv, ex);
            }
            WebUtils.exposeErrorRequestAttributes(request, ex, getServletName());
            return exMv;
        }

        throw ex;
    }

    /**
     * Render the given ModelAndView.
     * <p>This is the last stage in handling a request. It may involve resolving the view by name.
     *
     * @param mv       the ModelAndView to render
     * @param request  current HTTP servlet request
     * @param response current HTTP servlet response
     * @throws ServletException if view is missing or cannot be resolved
     * @throws Exception        if there's a problem rendering the view
     */
    protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Determine locale for request and apply it to the response.
        Locale locale = this.localeResolver.resolveLocale(request);
        response.setLocale(locale);

        View view;
        if (mv.isReference()) {
            // We need to resolve the view name.
            view = resolveViewName(mv.getViewName(), mv.getModelInternal(), locale, request);
            if (view == null) {
                throw new ServletException("Could not resolve view with name '" + mv.getViewName() +
                        "' in servlet with name '" + getServletName() + "'");
            }
        } else {
            // No need to lookup: the ModelAndView object contains the actual View object.
            view = mv.getView();
            if (view == null) {
                throw new ServletException("ModelAndView [" + mv + "] neither contains a view name nor a " +
                        "View object in servlet with name '" + getServletName() + "'");
            }
        }

        // Delegate to the View object for rendering.
        if (logger.isDebugEnabled()) {
            logger.debug("Rendering view [" + view + "] in DispatcherServlet with name '" + getServletName() + "'");
        }
        try {
            if (mv.getStatus() != null) {
                response.setStatus(mv.getStatus().value());
            }
            view.render(mv.getModelInternal(), request, response);
        } catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error rendering view [" + view + "] in DispatcherServlet with name '" +
                        getServletName() + "'", ex);
            }
            throw ex;
        }
    }

    /**
     * Translate the supplied request into a default view name.
     *
     * @param request current HTTP servlet request
     * @return the view name (or {@code null} if no default found)
     * @throws Exception if view name translation failed
     */
    protected String getDefaultViewName(HttpServletRequest request) throws Exception {
        return this.viewNameTranslator.getViewName(request);
    }

    /**
     * Resolve the given view name into a View object (to be rendered).
     * <p>The default implementations asks all ViewResolvers of this dispatcher.
     * Can be overridden for custom resolution strategies, potentially based on
     * specific model attributes or request parameters.
     *
     * @param viewName the name of the view to resolve
     * @param model    the model to be passed to the view
     * @param locale   the current locale
     * @param request  current HTTP servlet request
     * @return the View object, or {@code null} if none found
     * @throws Exception if the view cannot be resolved
     *                   (typically in case of problems creating an actual View object)
     * @see ViewResolver#resolveViewName
     */
    protected View resolveViewName(String viewName, Map<String, Object> model, Locale locale,
                                   HttpServletRequest request) throws Exception {

        for (ViewResolver viewResolver : this.viewResolvers) {
            View view = viewResolver.resolveViewName(viewName, locale);
            if (view != null) {
                return view;
            }
        }
        return null;
    }

    private void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response,
                                        HandlerExecutionChain mappedHandler, Exception ex) throws Exception {

        if (mappedHandler != null) {
            mappedHandler.triggerAfterCompletion(request, response, ex);
        }
        throw ex;
    }

    /**
     * Restore the request attributes after an include.
     *
     * @param request            current HTTP request
     * @param attributesSnapshot the snapshot of the request attributes before the include
     */
    @SuppressWarnings("unchecked")
    private void restoreAttributesAfterInclude(HttpServletRequest request, Map<?, ?> attributesSnapshot) {
        // Need to copy into separate Collection here, to avoid side effects
        // on the Enumeration when removing attributes.
        Set<String> attrsToCheck = new HashSet<String>();
        Enumeration<?> attrNames = request.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = (String) attrNames.nextElement();
            if (this.cleanupAfterInclude || attrName.startsWith(DEFAULT_STRATEGIES_PREFIX)) {
                attrsToCheck.add(attrName);
            }
        }

        // Add attributes that may have been removed
        attrsToCheck.addAll((Set<String>) attributesSnapshot.keySet());

        // Iterate over the attributes to check, restoring the original value
        // or removing the attribute, respectively, if appropriate.
        for (String attrName : attrsToCheck) {
            Object attrValue = attributesSnapshot.get(attrName);
            if (attrValue == null) {
                request.removeAttribute(attrName);
            } else if (attrValue != request.getAttribute(attrName)) {
                request.setAttribute(attrName, attrValue);
            }
        }
    }

    private static String getRequestUri(HttpServletRequest request) {
        String uri = (String) request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
        if (uri == null) {
            uri = request.getRequestURI();
        }
        return uri;
    }

}

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

package org.springframework.web.bind.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.cors.CorsConfiguration;

/**
 * Marks the annotated method or type as permitting cross origin requests.
 *
 * <p>By default all origins and headers are permitted, credentials are allowed,
 * and the maximum age is set to 1800 seconds (30 minutes). The list of HTTP
 * methods is set to the methods on the {@code @RequestMapping} if not
 * explicitly set on {@code @CrossOrigin}.
 *
 * <p><b>NOTE:</b> {@code @CrossOrigin} is processed if an appropriate
 * {@code HandlerMapping}-{@code HandlerAdapter} pair is configured such as the
 * {@code RequestMappingHandlerMapping}-{@code RequestMappingHandlerAdapter}
 * pair which are the default in the MVC Java config and the MVC namespace.
 * In particular {@code @CrossOrigin} is not supported with the
 * {@code DefaultAnnotationHandlerMapping}-{@code AnnotationMethodHandlerAdapter}
 * pair both of which are also deprecated.
 *
 * @author Russell Allen
 * @author Sebastien Deleuze
 * @author Sam Brannen
 * @since 4.2
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CrossOrigin {

	/**
	 * @deprecated as of Spring 4.3.4, in favor of using {@link CorsConfiguration#applyPermitDefaultValues}
	 */
	@Deprecated
	String[] DEFAULT_ORIGINS = { "*" };

	/**
	 * @deprecated as of Spring 4.3.4, in favor of using {@link CorsConfiguration#applyPermitDefaultValues}
	 */
	@Deprecated
	String[] DEFAULT_ALLOWED_HEADERS = { "*" };

	/**
	 * @deprecated as of Spring 4.3.4, in favor of using {@link CorsConfiguration#applyPermitDefaultValues}
	 */
	@Deprecated
	boolean DEFAULT_ALLOW_CREDENTIALS = true;

	/**
	 * @deprecated as of Spring 4.3.4, in favor of using {@link CorsConfiguration#applyPermitDefaultValues}
	 */
	@Deprecated
	long DEFAULT_MAX_AGE = 1800;


	/**
	 * Alias for {@link #origins}.
	 */
	@AliasFor("origins")
	String[] value() default {};

	/**
	 * List of allowed origins, e.g. {@code "https://domain1.com"}.
	 * <p>These values are placed in the {@code Access-Control-Allow-Origin}
	 * header of both the pre-flight response and the actual response.
	 * {@code "*"} means that all origins are allowed.
	 * <p>If undefined, all origins are allowed.
	 * <p><strong>Note:</strong> CORS checks use values from "Forwarded"
	 * (<a href="https://tools.ietf.org/html/rfc7239">RFC 7239</a>),
	 * "X-Forwarded-Host", "X-Forwarded-Port", and "X-Forwarded-Proto" headers,
	 * if present, in order to reflect the client-originated address.
	 * Consider using the {@code ForwardedHeaderFilter} in order to choose from a
	 * central place whether to extract and use, or to discard such headers.
	 * See the Spring Framework reference for more on this filter.
	 * @see #value
	 */
	@AliasFor("value")
	String[] origins() default {};

	/**
	 * List of request headers that can be used during the actual request.
	 * <p>This property controls the value of the pre-flight response's
	 * {@code Access-Control-Allow-Headers} header.
	 * {@code "*"}  means that all headers requested by the client are allowed.
	 * <p>If undefined, all requested headers are allowed.
	 */
	String[] allowedHeaders() default {};

	/**
	 * List of response headers that the user-agent will allow the client to access.
	 * <p>This property controls the value of actual response's
	 * {@code Access-Control-Expose-Headers} header.
	 * <p>If undefined, an empty exposed header list is used.
	 */
	String[] exposedHeaders() default {};

	/**
	 * List of supported HTTP request methods, e.g.
	 * {@code "{RequestMethod.GET, RequestMethod.POST}"}.
	 * <p>Methods specified here override those specified via {@code RequestMapping}.
	 * <p>If undefined, methods defined by {@link RequestMapping} annotation
	 * are used.
	 */
	RequestMethod[] methods() default {};

	/**
	 * Whether the browser should include any cookies associated with the
	 * domain of the request being annotated.
	 * <p>Set to {@code "false"} if such cookies should not included.
	 * An empty string ({@code ""}) means <em>undefined</em>.
	 * {@code "true"} means that the pre-flight response will include the header
	 * {@code Access-Control-Allow-Credentials=true}.
	 * <p>If undefined, credentials are allowed.
	 */
	String allowCredentials() default "";

	/**
	 * The maximum age (in seconds) of the cache duration for pre-flight responses.
	 * <p>This property controls the value of the {@code Access-Control-Max-Age}
	 * header in the pre-flight response.
	 * <p>Setting this to a reasonable value can reduce the number of pre-flight
	 * request/response interactions required by the browser.
	 * A negative value means <em>undefined</em>.
	 * <p>If undefined, max age is set to {@code 1800} seconds (i.e., 30 minutes).
	 */
	long maxAge() default -1;

}

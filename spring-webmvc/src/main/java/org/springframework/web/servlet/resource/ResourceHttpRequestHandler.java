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

package org.springframework.web.servlet.resource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRange;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.ResourceRegionHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.PathExtensionContentNegotiationStrategy;
import org.springframework.web.accept.ServletPathExtensionContentNegotiationStrategy;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.springframework.web.util.UrlPathHelper;

/**
 * {@code HttpRequestHandler} that serves static resources in an optimized way
 * according to the guidelines of Page Speed, YSlow, etc.
 *
 * <p>The {@linkplain #setLocations "locations"} property takes a list of Spring
 * {@link Resource} locations from which static resources are allowed to be served
 * by this handler. Resources could be served from a classpath location, e.g.
 * "classpath:/META-INF/public-web-resources/", allowing convenient packaging
 * and serving of resources such as .js, .css, and others in jar files.
 *
 * <p>This request handler may also be configured with a
 * {@link #setResourceResolvers(List) resourcesResolver} and
 * {@link #setResourceTransformers(List) resourceTransformer} chains to support
 * arbitrary resolution and transformation of resources being served. By default
 * a {@link PathResourceResolver} simply finds resources based on the configured
 * "locations". An application can configure additional resolvers and transformers
 * such as the {@link VersionResourceResolver} which can resolve and prepare URLs
 * for resources with a version in the URL.
 *
 * <p>This handler also properly evaluates the {@code Last-Modified} header
 * (if present) so that a {@code 304} status code will be returned as appropriate,
 * avoiding unnecessary overhead for resources that are already cached by the client.
 *
 * @author Keith Donald
 * @author Jeremy Grelle
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @author Brian Clozel
 * @author Rossen Stoyanchev
 * @since 3.0.4
 */
public class ResourceHttpRequestHandler extends WebContentGenerator
		implements HttpRequestHandler, EmbeddedValueResolverAware, InitializingBean, CorsConfigurationSource {

	// Servlet 3.1 setContentLengthLong(long) available?
	private static final boolean contentLengthLongAvailable =
			ClassUtils.hasMethod(ServletResponse.class, "setContentLengthLong", long.class);

	private static final Log logger = LogFactory.getLog(ResourceHttpRequestHandler.class);

	private static final String URL_RESOURCE_CHARSET_PREFIX = "[charset=";


	private final List<String> locationValues = new ArrayList<String>(4);

	private final List<Resource> locations = new ArrayList<Resource>(4);

	private final Map<Resource, Charset> locationCharsets = new HashMap<Resource, Charset>(4);

	private final List<ResourceResolver> resourceResolvers = new ArrayList<ResourceResolver>(4);

	private final List<ResourceTransformer> resourceTransformers = new ArrayList<ResourceTransformer>(4);

	private ResourceHttpMessageConverter resourceHttpMessageConverter;

	private ResourceRegionHttpMessageConverter resourceRegionHttpMessageConverter;

	private ContentNegotiationManager contentNegotiationManager;

	private PathExtensionContentNegotiationStrategy contentNegotiationStrategy;

	private CorsConfiguration corsConfiguration;

	private UrlPathHelper urlPathHelper;

	private StringValueResolver embeddedValueResolver;


	public ResourceHttpRequestHandler() {
		super(HttpMethod.GET.name(), HttpMethod.HEAD.name());
	}


	/**
	 * An alternative to {@link #setLocations(List)} that accepts a list of
	 * String-based location values, with support for {@link UrlResource}'s
	 * (e.g. files or HTTP URLs) with a special prefix to indicate the charset
	 * to use when appending relative paths. For example
	 * {@code "[charset=Windows-31J]https://example.org/path"}.
	 * @since 4.3.13
	 */
	public void setLocationValues(List<String> locationValues) {
		Assert.notNull(locationValues, "Location values list must not be null");
		this.locationValues.clear();
		this.locationValues.addAll(locationValues);
	}

	/**
	 * Set the {@code List} of {@code Resource} locations to use as sources
	 * for serving static resources.
	 * @see #setLocationValues(List)
	 */
	public void setLocations(List<Resource> locations) {
		Assert.notNull(locations, "Locations list must not be null");
		this.locations.clear();
		this.locations.addAll(locations);
	}

	/**
	 * Return the configured {@code List} of {@code Resource} locations.
	 * <p>Note that if {@link #setLocationValues(List) locationValues} are provided,
	 * instead of loaded Resource-based locations, this method will return
	 * empty until after initialization via {@link #afterPropertiesSet()}.
	 * @see #setLocationValues
	 * @see #setLocations
	 */
	public List<Resource> getLocations() {
		return this.locations;
	}

	/**
	 * Configure the list of {@link ResourceResolver}s to use.
	 * <p>By default {@link PathResourceResolver} is configured. If using this property,
	 * it is recommended to add {@link PathResourceResolver} as the last resolver.
	 */
	public void setResourceResolvers(List<ResourceResolver> resourceResolvers) {
		this.resourceResolvers.clear();
		if (resourceResolvers != null) {
			this.resourceResolvers.addAll(resourceResolvers);
		}
	}

	/**
	 * Return the list of configured resource resolvers.
	 */
	public List<ResourceResolver> getResourceResolvers() {
		return this.resourceResolvers;
	}

	/**
	 * Configure the list of {@link ResourceTransformer}s to use.
	 * <p>By default no transformers are configured for use.
	 */
	public void setResourceTransformers(List<ResourceTransformer> resourceTransformers) {
		this.resourceTransformers.clear();
		if (resourceTransformers != null) {
			this.resourceTransformers.addAll(resourceTransformers);
		}
	}

	/**
	 * Return the list of configured resource transformers.
	 */
	public List<ResourceTransformer> getResourceTransformers() {
		return this.resourceTransformers;
	}

	/**
	 * Configure the {@link ResourceHttpMessageConverter} to use.
	 * <p>By default a {@link ResourceHttpMessageConverter} will be configured.
	 * @since 4.3
	 */
	public void setResourceHttpMessageConverter(ResourceHttpMessageConverter messageConverter) {
		this.resourceHttpMessageConverter = messageConverter;
	}

	/**
	 * Return the configured resource converter.
	 * @since 4.3
	 */
	public ResourceHttpMessageConverter getResourceHttpMessageConverter() {
		return this.resourceHttpMessageConverter;
	}

	/**
	 * Configure the {@link ResourceRegionHttpMessageConverter} to use.
	 * <p>By default a {@link ResourceRegionHttpMessageConverter} will be configured.
	 * @since 4.3
	 */
	public void setResourceRegionHttpMessageConverter(ResourceRegionHttpMessageConverter messageConverter) {
		this.resourceRegionHttpMessageConverter = messageConverter;
	}

	/**
	 * Return the configured resource region converter.
	 * @since 4.3
	 */
	public ResourceRegionHttpMessageConverter getResourceRegionHttpMessageConverter() {
		return this.resourceRegionHttpMessageConverter;
	}

	/**
	 * Configure a {@code ContentNegotiationManager} to help determine the
	 * media types for resources being served. If the manager contains a path
	 * extension strategy it will be checked for registered file extension.
	 * @since 4.3
	 */
	public void setContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
		this.contentNegotiationManager = contentNegotiationManager;
	}

	/**
	 * Return the configured content negotiation manager.
	 * @since 4.3
	 */
	public ContentNegotiationManager getContentNegotiationManager() {
		return this.contentNegotiationManager;
	}

	/**
	 * Specify the CORS configuration for resources served by this handler.
	 * <p>By default this is not set in which allows cross-origin requests.
	 */
	public void setCorsConfiguration(CorsConfiguration corsConfiguration) {
		this.corsConfiguration = corsConfiguration;
	}

	/**
	 * Return the specified CORS configuration.
	 */
	@Override
	public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
		return this.corsConfiguration;
	}

	/**
	 * Provide a reference to the {@link UrlPathHelper} used to map requests to
	 * static resources. This helps to derive information about the lookup path
	 * such as whether it is decoded or not.
	 * @since 4.3.13
	 */
	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		this.urlPathHelper = urlPathHelper;
	}

	/**
	 * The configured {@link UrlPathHelper}.
	 * @since 4.3.13
	 */
	public UrlPathHelper getUrlPathHelper() {
		return this.urlPathHelper;
	}

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.embeddedValueResolver = resolver;
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		resolveResourceLocations();

		if (logger.isWarnEnabled() && CollectionUtils.isEmpty(this.locations)) {
			logger.warn("Locations list is empty. No resources will be served unless a " +
					"custom ResourceResolver is configured as an alternative to PathResourceResolver.");
		}

		if (this.resourceResolvers.isEmpty()) {
			this.resourceResolvers.add(new PathResourceResolver());
		}

		initAllowedLocations();

		if (this.resourceHttpMessageConverter == null) {
			this.resourceHttpMessageConverter = new ResourceHttpMessageConverter();
		}
		if (this.resourceRegionHttpMessageConverter == null) {
			this.resourceRegionHttpMessageConverter = new ResourceRegionHttpMessageConverter();
		}

		this.contentNegotiationStrategy = initContentNegotiationStrategy();
	}

	private void resolveResourceLocations() {
		if (CollectionUtils.isEmpty(this.locationValues)) {
			return;
		}
		else if (!CollectionUtils.isEmpty(this.locations)) {
			throw new IllegalArgumentException("Please set either Resource-based \"locations\" or " +
					"String-based \"locationValues\", but not both.");
		}

		ApplicationContext applicationContext = getApplicationContext();
		for (String location : this.locationValues) {
			if (this.embeddedValueResolver != null) {
				String resolvedLocation = this.embeddedValueResolver.resolveStringValue(location);
				if (resolvedLocation == null) {
					throw new IllegalArgumentException("Location resolved to null: " + location);
				}
				location = resolvedLocation;
			}
			Charset charset = null;
			location = location.trim();
			if (location.startsWith(URL_RESOURCE_CHARSET_PREFIX)) {
				int endIndex = location.indexOf(']', URL_RESOURCE_CHARSET_PREFIX.length());
				if (endIndex == -1) {
					throw new IllegalArgumentException("Invalid charset syntax in location: " + location);
				}
				String value = location.substring(URL_RESOURCE_CHARSET_PREFIX.length(), endIndex);
				charset = Charset.forName(value);
				location = location.substring(endIndex + 1);
			}
			Resource resource = applicationContext.getResource(location);
			this.locations.add(resource);
			if (charset != null) {
				if (!(resource instanceof UrlResource)) {
					throw new IllegalArgumentException("Unexpected charset for non-UrlResource: " + resource);
				}
				this.locationCharsets.put(resource, charset);
			}
		}
	}

	/**
	 * Look for a {@code PathResourceResolver} among the configured resource
	 * resolvers and set its {@code allowedLocations} property (if empty) to
	 * match the {@link #setLocations locations} configured on this class.
	 */
	protected void initAllowedLocations() {
		if (CollectionUtils.isEmpty(this.locations)) {
			return;
		}
		for (int i = getResourceResolvers().size() - 1; i >= 0; i--) {
			if (getResourceResolvers().get(i) instanceof PathResourceResolver) {
				PathResourceResolver pathResolver = (PathResourceResolver) getResourceResolvers().get(i);
				if (ObjectUtils.isEmpty(pathResolver.getAllowedLocations())) {
					pathResolver.setAllowedLocations(getLocations().toArray(new Resource[getLocations().size()]));
				}
				if (this.urlPathHelper != null) {
					pathResolver.setLocationCharsets(this.locationCharsets);
					pathResolver.setUrlPathHelper(this.urlPathHelper);
				}
				break;
			}
		}
	}

	/**
	 * Initialize the content negotiation strategy depending on the {@code ContentNegotiationManager}
	 * setup and the availability of a {@code ServletContext}.
	 * @see ServletPathExtensionContentNegotiationStrategy
	 * @see PathExtensionContentNegotiationStrategy
	 */
	protected PathExtensionContentNegotiationStrategy initContentNegotiationStrategy() {
		Map<String, MediaType> mediaTypes = null;
		if (getContentNegotiationManager() != null) {
			PathExtensionContentNegotiationStrategy strategy =
					getContentNegotiationManager().getStrategy(PathExtensionContentNegotiationStrategy.class);
			if (strategy != null) {
				mediaTypes = new HashMap<String, MediaType>(strategy.getMediaTypes());
			}
		}
		return (getServletContext() != null ?
				new ServletPathExtensionContentNegotiationStrategy(getServletContext(), mediaTypes) :
				new PathExtensionContentNegotiationStrategy(mediaTypes));
	}


	/**
	 * Processes a resource request.
	 * <p>Checks for the existence of the requested resource in the configured list of locations.
	 * If the resource does not exist, a {@code 404} response will be returned to the client.
	 * If the resource exists, the request will be checked for the presence of the
	 * {@code Last-Modified} header, and its value will be compared against the last-modified
	 * timestamp of the given resource, returning a {@code 304} status code if the
	 * {@code Last-Modified} value  is greater. If the resource is newer than the
	 * {@code Last-Modified} value, or the header is not present, the content resource
	 * of the resource will be written to the response with caching headers
	 * set to expire one year in the future.
	 */
	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// For very general mappings (e.g. "/") we need to check 404 first
		Resource resource = getResource(request);
		if (resource == null) {
			logger.trace("No matching resource found - returning 404");
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		if (HttpMethod.OPTIONS.matches(request.getMethod())) {
			response.setHeader("Allow", getAllowHeader());
			return;
		}

		// Supported methods and required session
		checkRequest(request);

		// Header phase
		if (new ServletWebRequest(request, response).checkNotModified(resource.lastModified())) {
			logger.trace("Resource not modified - returning 304");
			return;
		}

		// Apply cache settings, if any
		prepareResponse(response);

		// Check the media type for the resource
		MediaType mediaType = getMediaType(request, resource);
		if (mediaType != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Determined media type '" + mediaType + "' for " + resource);
			}
		}
		else {
			if (logger.isTraceEnabled()) {
				logger.trace("No media type found for " + resource + " - not sending a content-type header");
			}
		}

		// Content phase
		if (METHOD_HEAD.equals(request.getMethod())) {
			setHeaders(response, resource, mediaType);
			logger.trace("HEAD request - skipping content");
			return;
		}

		ServletServerHttpResponse outputMessage = new ServletServerHttpResponse(response);
		if (request.getHeader(HttpHeaders.RANGE) == null) {
			setHeaders(response, resource, mediaType);
			this.resourceHttpMessageConverter.write(resource, mediaType, outputMessage);
		}
		else {
			response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
			ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(request);
			try {
				List<HttpRange> httpRanges = inputMessage.getHeaders().getRange();
				response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
				if (httpRanges.size() == 1) {
					ResourceRegion resourceRegion = httpRanges.get(0).toResourceRegion(resource);
					this.resourceRegionHttpMessageConverter.write(resourceRegion, mediaType, outputMessage);
				}
				else {
					this.resourceRegionHttpMessageConverter.write(
							HttpRange.toResourceRegions(httpRanges, resource), mediaType, outputMessage);
				}
			}
			catch (IllegalArgumentException ex) {
				response.setHeader("Content-Range", "bytes */" + resource.contentLength());
				response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
			}
		}
	}

	protected Resource getResource(HttpServletRequest request) throws IOException {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		if (path == null) {
			throw new IllegalStateException("Required request attribute '" +
					HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE + "' is not set");
		}

		path = processPath(path);
		if (!StringUtils.hasText(path) || isInvalidPath(path)) {
			if (logger.isTraceEnabled()) {
				logger.trace("Ignoring invalid resource path [" + path + "]");
			}
			return null;
		}
		if (isInvalidEncodedPath(path)) {
			if (logger.isTraceEnabled()) {
				logger.trace("Ignoring invalid resource path with escape sequences [" + path + "]");
			}
			return null;
		}

		ResourceResolverChain resolveChain = new DefaultResourceResolverChain(getResourceResolvers());
		Resource resource = resolveChain.resolveResource(request, path, getLocations());
		if (resource == null || getResourceTransformers().isEmpty()) {
			return resource;
		}

		ResourceTransformerChain transformChain =
				new DefaultResourceTransformerChain(resolveChain, getResourceTransformers());
		resource = transformChain.transform(request, resource);
		return resource;
	}

	/**
	 * Process the given resource path.
	 * <p>The default implementation replaces:
	 * <ul>
	 * <li>Backslash with forward slash.
	 * <li>Duplicate occurrences of slash with a single slash.
	 * <li>Any combination of leading slash and control characters (00-1F and 7F)
	 * with a single "/" or "". For example {@code "  / // foo/bar"}
	 * becomes {@code "/foo/bar"}.
	 * </ul>
	 * @since 3.2.12
	 */
	protected String processPath(String path) {
		path = StringUtils.replace(path, "\\", "/");
		path = cleanDuplicateSlashes(path);
		return cleanLeadingSlash(path);
	}

	private String cleanDuplicateSlashes(String path) {
		StringBuilder sb = null;
		char prev = 0;
		for (int i = 0; i < path.length(); i++) {
			char curr = path.charAt(i);
			try {
				if ((curr == '/') && (prev == '/')) {
					if (sb == null) {
						sb = new StringBuilder(path.substring(0, i));
					}
					continue;
				}
				if (sb != null) {
					sb.append(path.charAt(i));
				}
			}
			finally {
				prev = curr;
			}
		}
		return sb != null ? sb.toString() : path;
	}

	private String cleanLeadingSlash(String path) {
		boolean slash = false;
		for (int i = 0; i < path.length(); i++) {
			if (path.charAt(i) == '/') {
				slash = true;
			}
			else if (path.charAt(i) > ' ' && path.charAt(i) != 127) {
				if (i == 0 || (i == 1 && slash)) {
					return path;
				}
				path = (slash ? "/" + path.substring(i) : path.substring(i));
				if (logger.isTraceEnabled()) {
					logger.trace("Path after trimming leading '/' and control characters: [" + path + "]");
				}
				return path;
			}
		}
		return (slash ? "/" : "");
	}

	/**
	 * Check whether the given path contains invalid escape sequences.
	 * @param path the path to validate
	 * @return {@code true} if the path is invalid, {@code false} otherwise
	 */
	private boolean isInvalidEncodedPath(String path) {
		if (path.contains("%")) {
			try {
				// Use URLDecoder (vs UriUtils) to preserve potentially decoded UTF-8 chars
				String decodedPath = URLDecoder.decode(path, "UTF-8");
				if (isInvalidPath(decodedPath)) {
					return true;
				}
				decodedPath = processPath(decodedPath);
				if (isInvalidPath(decodedPath)) {
					return true;
				}
			}
			catch (IllegalArgumentException ex) {
				// Should never happen...
			}
			catch (UnsupportedEncodingException ex) {
				// Should never happen...
			}
		}
		return false;
	}

	/**
	 * Identifies invalid resource paths. By default rejects:
	 * <ul>
	 * <li>Paths that contain "WEB-INF" or "META-INF"
	 * <li>Paths that contain "../" after a call to
	 * {@link org.springframework.util.StringUtils#cleanPath}.
	 * <li>Paths that represent a {@link org.springframework.util.ResourceUtils#isUrl
	 * valid URL} or would represent one after the leading slash is removed.
	 * </ul>
	 * <p><strong>Note:</strong> this method assumes that leading, duplicate '/'
	 * or control characters (e.g. white space) have been trimmed so that the
	 * path starts predictably with a single '/' or does not have one.
	 * @param path the path to validate
	 * @return {@code true} if the path is invalid, {@code false} otherwise
	 * @since 3.0.6
	 */
	protected boolean isInvalidPath(String path) {
		if (path.contains("WEB-INF") || path.contains("META-INF")) {
			if (logger.isTraceEnabled()) {
				logger.trace("Path with \"WEB-INF\" or \"META-INF\": [" + path + "]");
			}
			return true;
		}
		if (path.contains(":/")) {
			String relativePath = (path.charAt(0) == '/' ? path.substring(1) : path);
			if (ResourceUtils.isUrl(relativePath) || relativePath.startsWith("url:")) {
				if (logger.isTraceEnabled()) {
					logger.trace("Path represents URL or has \"url:\" prefix: [" + path + "]");
				}
				return true;
			}
		}
		if (path.contains("..") && StringUtils.cleanPath(path).contains("../")) {
			if (logger.isTraceEnabled()) {
				logger.trace("Path contains \"../\" after call to StringUtils#cleanPath: [" + path + "]");
			}
			return true;
		}
		return false;
	}

	/**
	 * Determine the media type for the given request and the resource matched
	 * to it. This implementation tries to determine the MediaType based on the
	 * file extension of the Resource via
	 * {@link ServletPathExtensionContentNegotiationStrategy#getMediaTypeForResource}.
	 * @param request the current request
	 * @param resource the resource to check
	 * @return the corresponding media type, or {@code null} if none found
	 */
	@SuppressWarnings("deprecation")
	protected MediaType getMediaType(HttpServletRequest request, Resource resource) {
		// For backwards compatibility
		MediaType mediaType = getMediaType(resource);
		if (mediaType != null) {
			return mediaType;
		}
		return this.contentNegotiationStrategy.getMediaTypeForResource(resource);
	}

	/**
	 * Determine an appropriate media type for the given resource.
	 * @param resource the resource to check
	 * @return the corresponding media type, or {@code null} if none found
	 * @deprecated as of 4.3 this method is deprecated; please override
	 * {@link #getMediaType(HttpServletRequest, Resource)} instead.
	 */
	@Deprecated
	protected MediaType getMediaType(Resource resource) {
		return null;
	}

	/**
	 * Set headers on the given servlet response.
	 * Called for GET requests as well as HEAD requests.
	 * @param response current servlet response
	 * @param resource the identified resource (never {@code null})
	 * @param mediaType the resource's media type (never {@code null})
	 * @throws IOException in case of errors while setting the headers
	 */
	protected void setHeaders(HttpServletResponse response, Resource resource, MediaType mediaType) throws IOException {
		long length = resource.contentLength();
		if (length > Integer.MAX_VALUE) {
			if (contentLengthLongAvailable) {
				response.setContentLengthLong(length);
			}
			else {
				response.setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(length));
			}
		}
		else {
			response.setContentLength((int) length);
		}

		if (mediaType != null) {
			response.setContentType(mediaType.toString());
		}
		if (resource instanceof EncodedResource) {
			response.setHeader(HttpHeaders.CONTENT_ENCODING, ((EncodedResource) resource).getContentEncoding());
		}
		if (resource instanceof VersionedResource) {
			response.setHeader(HttpHeaders.ETAG, "\"" + ((VersionedResource) resource).getVersion() + "\"");
		}
		response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
	}


	@Override
	public String toString() {
		return "ResourceHttpRequestHandler [locations=" + getLocations() + ", resolvers=" + getResourceResolvers() + "]";
	}

}

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

package org.springframework.web.method.annotation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionAttributeStore;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.WebRequest;

/**
 * 管理通过{@link SessionAttributes @SessionAttributes}声明的特定于控制器的会话属性。
 * 实际存储委托给{@link SessionAttributeStore}实例。
 *
 * 当一个使用了{@code @SessionAttributes}注解的控制器添加attributes到Model时，
 * 将根据通过{@code @SessionAttributes}指定的名称和类型检查这些属性。
 * 匹配的模型属性保存在HTTP session中并保持不变，直到控制器调用{@link SessionStatus＃setComplete（）}。
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 3.1
 */
public class SessionAttributesHandler {

	private final Set<String> attributeNames = new HashSet<String>();

	private final Set<Class<?>> attributeTypes = new HashSet<Class<?>>();

	private final Set<String> knownAttributeNames =
			Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(4));

	private final SessionAttributeStore sessionAttributeStore;


	/**
	 * 创建新的会话属性处理程序。 会话属性名称和类型是从给定类型的{@code @SessionAttributes}注释中提取的（如果存在）。
	 * @param handlerType the controller type
	 * @param sessionAttributeStore used for session access
	 */
	public SessionAttributesHandler(Class<?> handlerType, SessionAttributeStore sessionAttributeStore) {
		Assert.notNull(sessionAttributeStore, "SessionAttributeStore may not be null");
		this.sessionAttributeStore = sessionAttributeStore;

		SessionAttributes ann = AnnotatedElementUtils.findMergedAnnotation(handlerType, SessionAttributes.class);
		if (ann != null) {
			this.attributeNames.addAll(Arrays.asList(ann.names()));
			this.attributeTypes.addAll(Arrays.asList(ann.types()));
		}
		this.knownAttributeNames.addAll(this.attributeNames);
	}


	/**
	 * Whether the controller represented by this instance has declared any
	 * session attributes through an {@link SessionAttributes} annotation.
	 */
	public boolean hasSessionAttributes() {
		return (!this.attributeNames.isEmpty() || !this.attributeTypes.isEmpty());
	}

	/**
	 * 属性名称或类型是否与基础控制器上的{@code @SessionAttributes}指定的名称和类型相匹配。
	 * <p>Attributes successfully resolved through this method are "remembered"
	 * and subsequently used in {@link #retrieveAttributes(WebRequest)} and
	 * {@link #cleanupAttributes(WebRequest)}.
	 * @param attributeName the attribute name to check
	 * @param attributeType the type for the attribute
	 */
	public boolean isHandlerSessionAttribute(String attributeName, Class<?> attributeType) {
		Assert.notNull(attributeName, "Attribute name must not be null");
		if (this.attributeNames.contains(attributeName) || this.attributeTypes.contains(attributeType)) {
			this.knownAttributeNames.add(attributeName);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Store a subset of the given attributes in the session. Attributes not
	 * declared as session attributes via {@code @SessionAttributes} are ignored.
	 * @param request the current request
	 * @param attributes candidate attributes for session storage
	 */
	public void storeAttributes(WebRequest request, Map<String, ?> attributes) {
		for (String name : attributes.keySet()) {
			Object value = attributes.get(name);
			Class<?> attrType = (value != null ? value.getClass() : null);
			if (isHandlerSessionAttribute(name, attrType)) {
				this.sessionAttributeStore.storeAttribute(request, name, value);
			}
		}
	}

	/**
	 * 从会话中检索“已知”属性，即列出的属性
	 *   在{@code @SessionAttributes}中按名称或先前存储在按类型匹配的模型中的属性。
	 * @param request the current request
	 * @return a map with handler session attributes, possibly empty
	 */
	public Map<String, Object> retrieveAttributes(WebRequest request) {
		Map<String, Object> attributes = new HashMap<String, Object>();
		for (String name : this.knownAttributeNames) {
			Object value = this.sessionAttributeStore.retrieveAttribute(request, name);
			if (value != null) {
				attributes.put(name, value);
			}
		}
		return attributes;
	}

	/**
	 * Remove "known" attributes from the session, i.e. attributes listed
	 * by name in {@code @SessionAttributes} or attributes previously stored
	 * in the model that matched by type.
	 * @param request the current request
	 */
	public void cleanupAttributes(WebRequest request) {
		for (String attributeName : this.knownAttributeNames) {
			this.sessionAttributeStore.cleanupAttribute(request, attributeName);
		}
	}

	/**
	 * A pass-through call to the underlying {@link SessionAttributeStore}.
	 * @param request the current request
	 * @param attributeName the name of the attribute of interest
	 * @return the attribute value, or {@code null} if none
	 */
	Object retrieveAttribute(WebRequest request, String attributeName) {
		return this.sessionAttributeStore.retrieveAttribute(request, attributeName);
	}

}

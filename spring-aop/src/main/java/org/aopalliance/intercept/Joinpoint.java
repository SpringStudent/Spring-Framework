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

package org.aopalliance.intercept;

import java.lang.reflect.AccessibleObject;

/**
 *
 * 接口代表着通用的运行时连接点(AOP术语)
 *
 * 运行时连接点是发发生在程序某个位置的静态连接点.例如，调用是方法上的运行时连接点（静态连接点）
 * 可以一般地检索给定连接点的静态部分使用{@link #getStaticPart（）}方法。
 *
 * 在拦截框架的上下文中，运行时连接点是对可访问对象（方法，构造函数，字段）hod的访问的具体化。
 *
 * @author Rod Johnson
 * @see Interceptor
 */
public interface Joinpoint {

	/**
	 * 继续进入链中的下一个拦截器。
	 * <p>此方法的实现和语义取决于实际的连接点类型（请参阅子接口）
	 * @return see the children interfaces' proceed definition
	 * @throws Throwable if the joinpoint throws an exception
	 */
	Object proceed() throws Throwable;

	/**
	 * 返回保存当前连接点的静态部分的对象。
	 * <p>例如，调用的目标对象.
	 * @return the object (can be null if the accessible object is static)
	 */
	Object getThis();

	/**
	 * 返回此连接点的静态部分。
	 * <p>静态部分是一个可访问的对象，其上安装了一系列拦截器
	 */
	AccessibleObject getStaticPart();

}

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

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;

/**
 * {@link TransactionInterceptor}用于元数据检索的策略接口
 *
 * <p>Implementations know how to source transaction attributes, whether from configuration,
 * metadata attributes at source level (such as Java 5 annotations), or anywhere else.
 *
 * 实现知道如何从配置，源级别的元数据属性（例如Java 5注释）或其他任何地方获取事务属性
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 15.04.2003
 * @see TransactionInterceptor#setTransactionAttributeSource
 * @see TransactionProxyFactoryBean#setTransactionAttributeSource
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
 */
public interface TransactionAttributeSource {

	/**
	 * 返回给定方法的事务属性，如果方法是非事务性的，则返回{@code null}
	 * @param method the method to introspect
	 * @param targetClass the target class (may be {@code null},
	 * in which case the declaring class of the method must be used)
	 * @return the matching transaction attribute, or {@code null} if none found
	 */
	TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass);

}

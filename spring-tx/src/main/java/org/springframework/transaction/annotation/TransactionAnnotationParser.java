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

package org.springframework.transaction.annotation;

import java.lang.reflect.AnnotatedElement;

import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * 用于解析已知事务注释类型的策略接口。
 * {@link AnnotationTransactionAttributeSource}委托给他们
 * 用于支持特定注释类型的解析器，例如Spring自己的注释类型
 * {@link Transactional}，JTA 1.2的{@link javax.transaction.Transactional}
 * 或EJB3的{@link javax.ejb.TransactionAttribute}
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see AnnotationTransactionAttributeSource
 * @see SpringTransactionAnnotationParser
 * @see Ejb3TransactionAnnotationParser
 * @see JtaTransactionAnnotationParser
 */
public interface TransactionAnnotationParser {

	/**
	 * 解析给定方法或类的事务属性，
	 * 基于此解析器理解的注释类型
	 * <p>这实际上将已知的事务注释解析为Spring的元数据属性类。 如果方法/类不是事务性的，则返回{@code null}
	 * @param element the annotated method or class
	 * @return the configured transaction attribute, or {@code null} if none found
	 * @see AnnotationTransactionAttributeSource#determineTransactionAttribute
	 */
	TransactionAttribute parseTransactionAnnotation(AnnotatedElement element);

}

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

import org.springframework.transaction.TransactionDefinition;

/**
 *
 * 此接口将{@code rollbackOn}规范添加到{@link TransactionDefinition}。
 * 由于自定义{@code rollbackOn}仅适用于AOP，因此它位于与AOP相关的事务子包中
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16.03.2003
 * @see DefaultTransactionAttribute
 * @see RuleBasedTransactionAttribute
 */
public interface TransactionAttribute extends TransactionDefinition {

	/**
	 * 返回与此事务属性关联的限定符值。
	 *
	 * <p>这可以用于选择相应的事务管理器
	 * 处理此特定事务。
	 * @since 3.0
	 */
	String getQualifier();

	/**
	 * Should we roll back on the given exception?
	 * @param ex the exception to evaluate
	 * @return whether to perform a rollback or not
	 */
	boolean rollbackOn(Throwable ex);

}

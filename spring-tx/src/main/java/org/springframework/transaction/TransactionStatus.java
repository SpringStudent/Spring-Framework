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

package org.springframework.transaction;

import java.io.Flushable;

/**
 * 表示事务状态
 *
 * 事务代码可以用它来获取状态信息,并以编程方式请求回滚（而不是抛出
 * 导致隐式回滚的异常。
 *
 * 包括{@link SavepointManager}接口已提供savepoint管理特性.
 * 请注意，只有在基础事务管理器支持的情况下，保存点管理才可用。
 *
 * @author Juergen Hoeller
 * @since 27.03.2003
 * @see #setRollbackOnly()
 * @see PlatformTransactionManager#getTransaction
 * @see org.springframework.transaction.support.TransactionCallback#doInTransaction
 * @see org.springframework.transaction.interceptor.TransactionInterceptor#currentTransactionStatus()
 */
public interface TransactionStatus extends SavepointManager, Flushable {

	/**
	 * 返回当前事务是否为新的;否则加入已有事务,获取不能够原先事务中进行
	 */
	boolean isNewTransaction();

	/**
	 * 返回此事务内部是否带有保存点，
	 * 也就是说，已经基于保存点创建为嵌套事务。
	 * <p>此方法主要用于诊断目的
	 * {@link #isNewTransaction（）}。对于自定义保存点的编程处理，
	 * 请使用{@link SavepointManager}提供的操作。
	 * @see #isNewTransaction()
	 * @see #createSavepoint()
	 * @see #rollbackToSavepoint(Object)
	 * @see #releaseSavepoint(Object)
	 */
	boolean hasSavepoint();

	/**
	 * Set the transaction rollback-only. This instructs the transaction manager
	 * that the only possible outcome of the transaction may be a rollback, as
	 * alternative to throwing an exception which would in turn trigger a rollback.
	 * <p>This is mainly intended for transactions managed by
	 * {@link org.springframework.transaction.support.TransactionTemplate} or
	 * {@link org.springframework.transaction.interceptor.TransactionInterceptor},
	 * where the actual commit/rollback decision is made by the container.
	 * @see org.springframework.transaction.support.TransactionCallback#doInTransaction
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#rollbackOn
	 */
	void setRollbackOnly();

	/**
	 * 返回事务是否已标记为仅回滚
	 * （通过应用程序或通过事务基础结构）。
	 */
	boolean isRollbackOnly();

	/**
	 * 如果适用，将基础会话刷新到数据存储区：例如，所有受影响的Hibernate / JPA会话。
	 *
	 * <p>This is effectively just a hint and may be a no-op if the underlying
	 * transaction manager does not have a flush concept. A flush signal may
	 * get applied to the primary resource or to transaction synchronizations,
	 * depending on the underlying resource.
	 */
	@Override
	void flush();

	/**
	 * 返回此事务是否已完成，即是否已提交或回滚。
	 * @see PlatformTransactionManager#commit
	 * @see PlatformTransactionManager#rollback
	 */
	boolean isCompleted();

}

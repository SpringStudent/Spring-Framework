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

package org.springframework.transaction;

/**
 * 这是Spring的事务基础结构的中心接口。.
 * 应用程序可以直接使用它，但它主要不是API：
 * 通常，应用程序可以使用TransactionTemplate或通过AOP进行声明性事务。
 *
 * 对于实现者，建议基于
 * {@link org.springframework.transaction.support.AbstractPlatformTransactionManager}类，
 * 它预先实现定义的传播行为并负责事务同步处理
 * 子类必须为底层事务的特定状态实现模板方法，例如：begin，suspend，resume，commit
 *
 * 可以做为其他事务实现指南的策略类默认实现类有
 *{@link org.springframework.transaction.jta.JtaTransactionManager}
 *{@link org.springframework.jdbc.datasource.DataSourceTransactionManager},
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16.05.2003
 * @see org.springframework.transaction.support.TransactionTemplate
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 */
public interface PlatformTransactionManager {

	/**
	 * 根据指定的传播行为返回当前活动的事务或者创建一个新的,
	 * <p>请注意，隔离级别或超时等参数仅适用于新事务，因此在参与活动事务时会被忽略。
	 *
	 * <p>此外，并非每个事务管理器支持事务定义设置
	 * ：当遇到不支持的设置时，正确的事务管理器实现应该抛出异常。
	 *
	 * <p>上述规则的一个例外是只读标志，应该是
	 * 如果不支持显式只读模式，则忽略。 从本质上讲，只读标志只是潜在优化的提示。
	 * @param definition the TransactionDefinition instance (can be {@code null} for defaults),
	 * describing propagation behavior, isolation level, timeout etc.
	 * @return transaction status object representing the new or current transaction
	 * @throws TransactionException in case of lookup, creation, or system errors
	 * @throws IllegalTransactionStateException if the given transaction definition
	 * cannot be executed (for example, if a currently active transaction is in
	 * conflict with the specified propagation behavior)
	 * @see TransactionDefinition#getPropagationBehavior
	 * @see TransactionDefinition#getIsolationLevel
	 * @see TransactionDefinition#getTimeout
	 * @see TransactionDefinition#isReadOnly
	 */
	TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException;

	/**
	 * 就给定事务的状态提交给定事务。 如果事务已以编程方式标记为仅回滚，请执行回滚。
	 *
	 * 如果事务不是新建的那个,为了正确参与周边的事务忽略提交.如果为了能够创建一个新的事务
	 * 将先前事务挂起了,在提交新创建的事务后继续先前的事务
	 *

	 * 请注意；当commit调用完成,无论是正常还是异常,事务必须完全完成并且被清除.
	 * 在这种情形下不应会滚调用
	 *
	 * 如果此方法抛出除TransactionException之外的异常，一些提前提交错误导致提交尝试失败.
	 * 例如，O / R Mapping工具可能在提交之前尝试刷新对数据库的更改，结果是DataAccessException导致事务失败
	 * 在这种情况下，原始异常将传播给此提交方法的调用者。
	 *
	 * @param status object returned by the {@code getTransaction} method
	 * @throws UnexpectedRollbackException in case of an unexpected rollback
	 * that the transaction coordinator initiated
	 * @throws HeuristicCompletionException in case of a transaction failure
	 * caused by a heuristic decision on the side of the transaction coordinator
	 * @throws TransactionSystemException in case of commit or system errors
	 * (typically caused by fundamental resource failures)
	 * @throws IllegalTransactionStateException if the given transaction
	 * is already completed (that is, committed or rolled back)
	 * @see TransactionStatus#setRollbackOnly
	 */
	void commit(TransactionStatus status) throws TransactionException;

	/**
	 * 执行给定事务的回滚。
	 *
	 * 如果事务不是新事务，为了正确参与周边事务仅仅设置rollback-only.如果为了能够创建一个新的事务
	 * 	将先前事务挂起了,在回滚新事务后恢复上一个事务。
	 *
	 * 如果提交引发异常，请不要在事务上调用回滚。
	 * 即使在提交异常的情况下，事务也将在提交返回时完成并清除。
	 * 因此，提交失败后的回滚调用将导致IllegalTransactionStateException。
	 * @param status object returned by the {@code getTransaction} method
	 * @throws TransactionSystemException in case of rollback or system errors
	 * (typically caused by fundamental resource failures)
	 * @throws IllegalTransactionStateException if the given transaction
	 * is already completed (that is, committed or rolled back)
	 */
	void rollback(TransactionStatus status) throws TransactionException;

}

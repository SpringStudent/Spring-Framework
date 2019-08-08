/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.transaction.support;

import java.io.Flushable;

/**
 * 有能够返回一个内部回滚标记的事务对象实现,
 * 通常来自另一个已参与的事务并将其标记为仅回滚。
 *
 * <p>Autodetected by DefaultTransactionStatus, to always return a
 * current rollbackOnly flag even if not resulting from the current
 * TransactionStatus.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see DefaultTransactionStatus#isRollbackOnly
 */
public interface SmartTransactionObject extends Flushable {

	/**
	 * 返回事务是否在内部标记为仅回滚。
	 * 例如，可以检查JTA UserTransaction。
	 * @see javax.transaction.UserTransaction#getStatus
	 * @see javax.transaction.Status#STATUS_MARKED_ROLLBACK
	 */
	boolean isRollbackOnly();

	/**
	 * Flush the underlying sessions to the datastore, if applicable:
	 * for example, all affected Hibernate/JPA sessions.
	 */
	@Override
	void flush();

}

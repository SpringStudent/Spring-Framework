/*
 * Copyright 2002-2012 the original author or authors.
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

import org.aopalliance.aop.Advice;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;

/**
 * Advisor driven by a {@link TransactionAttributeSource}, used to include
 * a {@link TransactionInterceptor} only for methods that are transactional.
 *
 * 由{@link TransactionAttributeSource}驱动的顾问，仅用于事务性方法包含{@link TransactionInterceptor}
 *
 * <p>Because the AOP framework caches advice calculations, this is normally
 * faster than just letting the TransactionInterceptor run and find out
 * itself that it has no work to do.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setTransactionInterceptor
 * @see TransactionProxyFactoryBean
 */
@SuppressWarnings("serial")
public class TransactionAttributeSourceAdvisor extends AbstractPointcutAdvisor {

	private TransactionInterceptor transactionInterceptor;

	private final TransactionAttributeSourcePointcut pointcut = new TransactionAttributeSourcePointcut() {
		@Override
		protected TransactionAttributeSource getTransactionAttributeSource() {
			return (transactionInterceptor != null ? transactionInterceptor.getTransactionAttributeSource() : null);
		}
	};


	/**
	 * Create a new TransactionAttributeSourceAdvisor.
	 */
	public TransactionAttributeSourceAdvisor() {
	}

	/**
	 * Create a new TransactionAttributeSourceAdvisor.
	 * @param interceptor the transaction interceptor to use for this advisor
	 */
	public TransactionAttributeSourceAdvisor(TransactionInterceptor interceptor) {
		setTransactionInterceptor(interceptor);
	}


	/**
	 * Set the transaction interceptor to use for this advisor.
	 */
	public void setTransactionInterceptor(TransactionInterceptor interceptor) {
		this.transactionInterceptor = interceptor;
	}

	/**
	 * Set the {@link ClassFilter} to use for this pointcut.
	 * Default is {@link ClassFilter#TRUE}.
	 */
	public void setClassFilter(ClassFilter classFilter) {
		this.pointcut.setClassFilter(classFilter);
	}


	@Override
	public Advice getAdvice() {
		return this.transactionInterceptor;
	}

	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

}

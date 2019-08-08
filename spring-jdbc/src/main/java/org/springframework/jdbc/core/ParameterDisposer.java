/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.jdbc.core;

/**
 * 由可以关闭资源的对象实现的接口由{@code SqlLobValue}对象等参数分配
 *
 * <p>Typically implemented by {@code PreparedStatementCreators} and
 * {@code PreparedStatementSetters} that support {@link DisposableSqlTypeValue}
 * objects (e.g. {@code SqlLobValue}) as parameters.
 *
 * 通常由支持 {@link DisposableSqlTypeValue}对象作为参数的{@code PreparedStatementCreators} and
 *  {@code PreparedStatementSetters}实现
 *
 * 通常由{@code PreparedStatementCreators}和
 * {@code PreparedStatementSetters}支持{@link DisposableSqlTypeValue}对象（例如{@code SqlLobValue}）作为参数
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.1
 * @see PreparedStatementCreator
 * @see PreparedStatementSetter
 * @see DisposableSqlTypeValue
 * @see org.springframework.jdbc.core.support.SqlLobValue
 */
public interface ParameterDisposer {

	/**
	 * 关闭那些 objects hold参数占有的资源，比如DisposableSqlTypeValue
	 * （比如SqlLobValue）
	 *
	 * @see DisposableSqlTypeValue#cleanup()
	 * @see org.springframework.jdbc.core.support.SqlLobValue#cleanup()
	 */
	void cleanupParameters();

}

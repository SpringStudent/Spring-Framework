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

package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * JdbcTemplate类使用的两个中央回调接口之一。此接口创建一个由JdbcTemplate类提供的给定连接的PreparedStatement。
 * 实现负责提供SQL和任何必要的参数
 *
 * 实现<i>不</ i>需要关注可能从它们尝试的操作中抛出的SQLExceptions。
 * JdbcTemplate类将适当地捕获和处理SQLExceptions
 *
 * <p>如果PreparedStatementCreator能够提供它用于创建PreparedStatement的SQL，
 * 它还应该实现SqlProvider接口。 如果出现异常，这可以提供更好的上下文信息
 *
 * @author Rod Johnson
 * @see JdbcTemplate#execute(PreparedStatementCreator, PreparedStatementCallback)
 * @see JdbcTemplate#query(PreparedStatementCreator, RowCallbackHandler)
 * @see JdbcTemplate#update(PreparedStatementCreator)
 * @see SqlProvider
 */
public interface PreparedStatementCreator {

	/**
	 * 在此连接中创建语句。 允许实现使用PreparedStatements。 JdbcTemplate将关闭创建的语句
	 * @param con Connection to use to create statement
	 * @return a prepared statement
	 * @throws SQLException there is no need to catch SQLExceptions
	 * that may be thrown in the implementation of this method.
	 * The JdbcTemplate class will handle them.
	 */
	PreparedStatement createPreparedStatement(Connection con) throws SQLException;

}

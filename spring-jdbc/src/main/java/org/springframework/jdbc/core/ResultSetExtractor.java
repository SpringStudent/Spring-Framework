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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;

/**
 * {@link JdbcTemplate}的查询方法使用的回调接口.
 * 此接口的实现执行从{@link java.sql.ResultSet}提取结果的实际工作，但不需要担心异常处理.
 * {@link java.sql.SQLException SQLExceptions}
 * 将被调用JdbcTemplate捕获并处理
 *
 * <p>该接口主要用于JDBC框架本身.
 * {@link RowMapper}通常是ResultSet处理的一个更简单的选择，每行映射一个结果对象而不是整个ResultSet的一个结果对象
 *
 * <p>注意：与{@link RowCallbackHandler}相比，ResultSetExtractor对象通常是无状态的，因此可重用, as long as it doesn't
 * 只要它不访问有状态资源（例如流式传输LOB内容时的输出流）或在对象内保持结果状态.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since April 24, 2003
 * @see JdbcTemplate
 * @see RowCallbackHandler
 * @see RowMapper
 * @see org.springframework.jdbc.core.support.AbstractLobStreamingResultSetExtractor
 */
public interface ResultSetExtractor<T> {

	/**
	 * 实现必须实现此方法来处理整个ResultSet.
	 * @param rs ResultSet to extract data from. Implementations should
	 * not close this: it will be closed by the calling JdbcTemplate.
	 * @return an arbitrary result object, or {@code null} if none
	 * (the extractor will typically be stateful in the latter case).
	 * @throws SQLException if a SQLException is encountered getting column
	 * values or navigating (that is, there's no need to catch SQLException)
	 * @throws DataAccessException in case of custom exceptions
	 */
	T extractData(ResultSet rs) throws SQLException, DataAccessException;

}

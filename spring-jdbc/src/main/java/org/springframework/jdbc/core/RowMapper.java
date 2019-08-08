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

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * 被{@link JdbcTemplate}使用:基于per-row映射{@link java.sql.ResultSet}所有行
 * 此接口的实现执行将每行映射到结果对象的实际工作，但不需要担心异常处理
 * {@link java.sql.SQLException SQLExceptions}将会被JdbcTemplate捕获并处理
 *
 * <p>通常用于{@link JdbcTemplate}的查询方法或存储过程的out参数。 RowMapper对象通常是无状态的，因此可以重用;
 * 它们是在一个地方实现行映射逻辑的理想选择
 *
 * <p>Alternatively, consider subclassing
 * {@link org.springframework.jdbc.object.MappingSqlQuery} from the
 * {@code jdbc.object} package: Instead of working with separate
 * JdbcTemplate and RowMapper objects, you can build executable query
 * objects (containing row-mapping logic) in that style.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @see JdbcTemplate
 * @see RowCallbackHandler
 * @see ResultSetExtractor
 * @see org.springframework.jdbc.object.MappingSqlQuery
 */
public interface RowMapper<T> {

	/**
	 * 实现必须实现此方法以映射ResultSet中的每一行数据。 此方法不应调用{@code next（）}
	 * ResultSet; 它只应映射当前行的值
	 * @param rs the ResultSet to map (pre-initialized for the current row)
	 * @param rowNum the number of the current row
	 * @return the result object for the current row (may be {@code null})
	 * @throws SQLException if a SQLException is encountered getting
	 * column values (that is, there's no need to catch SQLException)
	 */
	T mapRow(ResultSet rs, int rowNum) throws SQLException;

}

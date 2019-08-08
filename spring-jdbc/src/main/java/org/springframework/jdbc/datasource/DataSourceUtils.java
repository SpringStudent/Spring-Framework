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

package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * Helper class that provides static methods for obtaining JDBC Connections from
 * a {@link javax.sql.DataSource}. Includes special support for Spring-managed
 * transactional Connections, e.g. managed by {@link DataSourceTransactionManager}
 * or {@link org.springframework.transaction.jta.JtaTransactionManager}.
 *
 * <p>Used internally by Spring's {@link org.springframework.jdbc.core.JdbcTemplate},
 * Spring's JDBC operation objects and the JDBC {@link DataSourceTransactionManager}.
 * Can also be used directly in application code.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getConnection
 * @see #releaseConnection
 * @see DataSourceTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see org.springframework.transaction.support.TransactionSynchronizationManager
 */
public abstract class DataSourceUtils {

	/**
	 * Order value for TransactionSynchronization objects that clean up JDBC Connections.
	 */
	public static final int CONNECTION_SYNCHRONIZATION_ORDER = 1000;

	private static final Log logger = LogFactory.getLog(DataSourceUtils.class);


	/**
	 * 从给定的DataSource获取连接.将SQLExceptions转换为未经检查的通用数据访问异常的Spring层次结构，
	 * 简化调用代码并使任何抛出的异常更有意义.
	 *
	 * 例如当使用{@link DataSourceTransactionManager}能够知道绑定当前线程的connection.
	 * 如果事务同步处于活动状态，则将Connection绑定到线程，
	 * 例如 在{@link org.springframework.transaction.jta.JtaTransactionManager JTA}事务中运行时
	 * @param dataSource the DataSource to obtain Connections from
	 * @return a JDBC Connection from the given DataSource
	 * @throws org.springframework.jdbc.CannotGetJdbcConnectionException
	 * if the attempt to get a Connection failed
	 * @see #releaseConnection
	 */
	public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
		try {
			return doGetConnection(dataSource);
		}
		catch (SQLException ex) {
			throw new CannotGetJdbcConnectionException("Could not get JDBC Connection", ex);
		}
	}

	/**
	 * 真正的从给定的DataSource获取JDBC连接实现
	 * 与{@link #getConnection}相同，但抛出原始的SQLException.
	 * <p>Is aware of a corresponding Connection bound to the current thread, for example
	 * when using {@link DataSourceTransactionManager}. Will bind a Connection to the thread
	 * if transaction synchronization is active (e.g. if in a JTA transaction).
	 * 由{@link TransactionAwareDataSourceProxy}直接访问
	 * @param dataSource the DataSource to obtain Connections from
	 * @return a JDBC Connection from the given DataSource
	 * @throws SQLException if thrown by JDBC methods
	 * @see #doReleaseConnection
	 */
	public static Connection doGetConnection(DataSource dataSource) throws SQLException {
		Assert.notNull(dataSource, "No DataSource specified");

		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		if (conHolder != null && (conHolder.hasConnection() || conHolder.isSynchronizedWithTransaction())) {
			//引用+1
			conHolder.requested();
			//如果没有connection,设置一个进去
			if (!conHolder.hasConnection()) {
				logger.debug("Fetching resumed JDBC Connection from DataSource");
				conHolder.setConnection(dataSource.getConnection());
			}
			//返回当前线程的connection
			return conHolder.getConnection();
		}
		// Else we either got no holder or an empty thread-bound holder here.

		//从dataSource中获取连接
		logger.debug("Fetching JDBC Connection from DataSource");
		Connection con = dataSource.getConnection();

		//需要开启事务同步，创建connectionHolder保存connection
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			try {
				// Use same Connection for further JDBC actions within the transaction.
				// Thread-bound object will get removed by synchronization at transaction completion.
				//对事务中的其他JDBC操作使用相同的Connection。
				//在事务完成时，将通过同步删除线程绑定对象。
				ConnectionHolder holderToUse = conHolder;
				if (holderToUse == null) {
					holderToUse = new ConnectionHolder(con);
				}
				else {
					holderToUse.setConnection(con);
				}
				//引用数量+1
				holderToUse.requested();
				TransactionSynchronizationManager.registerSynchronization(
						new ConnectionSynchronization(holderToUse, dataSource));
				holderToUse.setSynchronizedWithTransaction(true);
				//绑定holder到当前线程上 key-dataSource value holderToUse
				if (holderToUse != conHolder) {
					TransactionSynchronizationManager.bindResource(dataSource, holderToUse);
				}
			}
			catch (RuntimeException ex) {
				// Unexpected exception from external delegation call -> close Connection and rethrow.
				releaseConnection(con, dataSource);
				throw ex;
			}
		}
		//不需要直接返回connection连接即可
		return con;
	}

	/**
	 * 使用给定的事务语义准备给定的Connection。
	 * @param con the Connection to prepare
	 * @param definition the transaction definition to apply
	 * @return the previous isolation level, if any
	 * @throws SQLException if thrown by JDBC methods
	 * @see #resetConnectionAfterTransaction
	 */
	public static Integer prepareConnectionForTransaction(Connection con, TransactionDefinition definition)
			throws SQLException {

		Assert.notNull(con, "No Connection specified");

		// Set read-only flag.
		if (definition != null && definition.isReadOnly()) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Setting JDBC Connection [" + con + "] read-only");
				}
				con.setReadOnly(true);
			}
			catch (SQLException ex) {
				Throwable exToCheck = ex;
				while (exToCheck != null) {
					if (exToCheck.getClass().getSimpleName().contains("Timeout")) {
						// Assume it's a connection timeout that would otherwise get lost: e.g. from JDBC 4.0
						throw ex;
					}
					exToCheck = exToCheck.getCause();
				}
				// "read-only not supported" SQLException -> ignore, it's just a hint anyway
				logger.debug("Could not set JDBC Connection read-only", ex);
			}
			catch (RuntimeException ex) {
				Throwable exToCheck = ex;
				while (exToCheck != null) {
					if (exToCheck.getClass().getSimpleName().contains("Timeout")) {
						// Assume it's a connection timeout that would otherwise get lost: e.g. from Hibernate
						throw ex;
					}
					exToCheck = exToCheck.getCause();
				}
				// "read-only not supported" UnsupportedOperationException -> ignore, it's just a hint anyway
				logger.debug("Could not set JDBC Connection read-only", ex);
			}
		}

		// Apply specific isolation level, if any.
		// 如果有的话设置隔离级别
		Integer previousIsolationLevel = null;
		if (definition != null && definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
			if (logger.isDebugEnabled()) {
				logger.debug("Changing isolation level of JDBC Connection [" + con + "] to " +
						definition.getIsolationLevel());
			}
			int currentIsolation = con.getTransactionIsolation();
			if (currentIsolation != definition.getIsolationLevel()) {
				previousIsolationLevel = currentIsolation;
				con.setTransactionIsolation(definition.getIsolationLevel());
			}
		}

		return previousIsolationLevel;
	}

	/**
	 * 在事务完成后重置给定的连接，
	 *   关于只读标志和隔离级别。
	 * @param con the Connection to reset
	 * @param previousIsolationLevel the isolation level to restore, if any
	 * @see #prepareConnectionForTransaction
	 */
	public static void resetConnectionAfterTransaction(Connection con, Integer previousIsolationLevel) {
		Assert.notNull(con, "No Connection specified");
		try {
			// Reset transaction isolation to previous value, if changed for the transaction.
			if (previousIsolationLevel != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Resetting isolation level of JDBC Connection [" +
							con + "] to " + previousIsolationLevel);
				}
				con.setTransactionIsolation(previousIsolationLevel);
			}

			// Reset read-only flag.
			if (con.isReadOnly()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Resetting read-only flag of JDBC Connection [" + con + "]");
				}
				con.setReadOnly(false);
			}
		}
		catch (Throwable ex) {
			logger.debug("Could not reset JDBC Connection after transaction", ex);
		}
	}

	/**
	 * Determine whether the given JDBC Connection is transactional, that is,
	 * bound to the current thread by Spring's transaction facilities.
	 * @param con the Connection to check
	 * @param dataSource the DataSource that the Connection was obtained from
	 * (may be {@code null})
	 * @return whether the Connection is transactional
	 */
	public static boolean isConnectionTransactional(Connection con, DataSource dataSource) {
		if (dataSource == null) {
			return false;
		}
		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		return (conHolder != null && connectionEquals(conHolder, con));
	}

	/**
	 * Apply the current transaction timeout, if any,
	 * to the given JDBC Statement object.
	 * @param stmt the JDBC Statement object
	 * @param dataSource the DataSource that the Connection was obtained from
	 * @throws SQLException if thrown by JDBC methods
	 * @see java.sql.Statement#setQueryTimeout
	 */
	public static void applyTransactionTimeout(Statement stmt, DataSource dataSource) throws SQLException {
		applyTimeout(stmt, dataSource, -1);
	}

	/**
	 * 应用指定的timeout-如果current transaction有超时时间的话设置给statement
	 * 否则使用传递的
	 * @param stmt the JDBC Statement object
	 * @param dataSource the DataSource that the Connection was obtained from
	 * @param timeout the timeout to apply (or 0 for no timeout outside of a transaction)
	 * @throws SQLException if thrown by JDBC methods
	 * @see java.sql.Statement#setQueryTimeout
	 */
	public static void applyTimeout(Statement stmt, DataSource dataSource, int timeout) throws SQLException {
		Assert.notNull(stmt, "No Statement specified");
		ConnectionHolder holder = null;
		//获取Connectionholder
		if (dataSource != null) {
			holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		}
		//获取holder的timeOut时间并设置
		if (holder != null && holder.hasTimeout()) {
			// Remaining transaction timeout overrides specified value.
			stmt.setQueryTimeout(holder.getTimeToLiveInSeconds());
		}
		else if (timeout >= 0) {
			// No current transaction timeout -> apply specified value.
			stmt.setQueryTimeout(timeout);
		}
	}

	/**
	 *
	 * 关闭从给定DataSource获取的给定Connection，如果它不是外部管理的（也就是说，没有绑定到线程）
	 * @param con the Connection to close if necessary
	 * (if this is {@code null}, the call will be ignored)
	 * @param dataSource the DataSource that the Connection was obtained from
	 * (may be {@code null})
	 * @see #getConnection
	 */
	public static void releaseConnection(Connection con, DataSource dataSource) {
		try {
			doReleaseConnection(con, dataSource);
		}
		catch (SQLException ex) {
			logger.debug("Could not close JDBC Connection", ex);
		}
		catch (Throwable ex) {
			logger.debug("Unexpected exception on closing JDBC Connection", ex);
		}
	}

	/**
	 * 实际上关闭从给定DataSource获取的给定Connection。 与{@link #releaseConnection}相同，但抛出原始的SQLException
	 * <p>Directly accessed by {@link TransactionAwareDataSourceProxy}.
	 * @param con the Connection to close if necessary
	 * (if this is {@code null}, the call will be ignored)
	 * @param dataSource the DataSource that the Connection was obtained from
	 * (may be {@code null})
	 * @throws SQLException if thrown by JDBC methods
	 * @see #doGetConnection
	 */
	public static void doReleaseConnection(Connection con, DataSource dataSource) throws SQLException {
		if (con == null) {
			return;
		}
		if (dataSource != null) {
			//获取dataSource对应的ConnectionHolder,如果还有别人在占用该连接应该调用released方法，而不是真正释放连接
			ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
			if (conHolder != null && connectionEquals(conHolder, con)) {
				// It's the transactional Connection: Don't close it.
				// 不要关闭处理带有事务的连接
				conHolder.released();
				return;
			}
		}
		doCloseConnection(con, dataSource);
	}

	/**
	 * Close the Connection, unless a {@link SmartDataSource} doesn't want us to.
	 * @param con the Connection to close if necessary
	 * @param dataSource the DataSource that the Connection was obtained from
	 * @throws SQLException if thrown by JDBC methods
	 * @see Connection#close()
	 * @see SmartDataSource#shouldClose(Connection)
	 */
	public static void doCloseConnection(Connection con, DataSource dataSource) throws SQLException {
		if (!(dataSource instanceof SmartDataSource) || ((SmartDataSource) dataSource).shouldClose(con)) {
			con.close();
		}
	}

	/**
	 * Determine whether the given two Connections are equal, asking the target
	 * Connection in case of a proxy. Used to detect equality even if the
	 * user passed in a raw target Connection while the held one is a proxy.
	 * @param conHolder the ConnectionHolder for the held Connection (potentially a proxy)
	 * @param passedInCon the Connection passed-in by the user
	 * (potentially a target Connection without proxy)
	 * @return whether the given Connections are equal
	 * @see #getTargetConnection
	 */
	private static boolean connectionEquals(ConnectionHolder conHolder, Connection passedInCon) {
		if (!conHolder.hasConnection()) {
			return false;
		}
		Connection heldCon = conHolder.getConnection();
		// Explicitly check for identity too: for Connection handles that do not implement
		// "equals" properly, such as the ones Commons DBCP exposes).
		return (heldCon == passedInCon || heldCon.equals(passedInCon) ||
				getTargetConnection(heldCon).equals(passedInCon));
	}

	/**
	 * Return the innermost target Connection of the given Connection. If the given
	 * Connection is a proxy, it will be unwrapped until a non-proxy Connection is
	 * found. Otherwise, the passed-in Connection will be returned as-is.
	 * @param con the Connection proxy to unwrap
	 * @return the innermost target Connection, or the passed-in one if no proxy
	 * @see ConnectionProxy#getTargetConnection()
	 */
	public static Connection getTargetConnection(Connection con) {
		Connection conToUse = con;
		while (conToUse instanceof ConnectionProxy) {
			conToUse = ((ConnectionProxy) conToUse).getTargetConnection();
		}
		return conToUse;
	}

	/**
	 * Determine the connection synchronization order to use for the given
	 * DataSource. Decreased for every level of nesting that a DataSource
	 * has, checked through the level of DelegatingDataSource nesting.
	 * @param dataSource the DataSource to check
	 * @return the connection synchronization order to use
	 * @see #CONNECTION_SYNCHRONIZATION_ORDER
	 */
	private static int getConnectionSynchronizationOrder(DataSource dataSource) {
		int order = CONNECTION_SYNCHRONIZATION_ORDER;
		DataSource currDs = dataSource;
		while (currDs instanceof DelegatingDataSource) {
			order--;
			currDs = ((DelegatingDataSource) currDs).getTargetDataSource();
		}
		return order;
	}


	/**
	 * 在非本机JDBC事务结束时回调资源清理
	 * (e.g. when participating in a JtaTransactionManager transaction).
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	private static class ConnectionSynchronization extends TransactionSynchronizationAdapter {

		private final ConnectionHolder connectionHolder;

		private final DataSource dataSource;

		private int order;

		private boolean holderActive = true;

		public ConnectionSynchronization(ConnectionHolder connectionHolder, DataSource dataSource) {
			this.connectionHolder = connectionHolder;
			this.dataSource = dataSource;
			this.order = getConnectionSynchronizationOrder(dataSource);
		}

		@Override
		public int getOrder() {
			return this.order;
		}

		@Override
		public void suspend() {
			if (this.holderActive) {
				TransactionSynchronizationManager.unbindResource(this.dataSource);
				if (this.connectionHolder.hasConnection() && !this.connectionHolder.isOpen()) {
					// Release Connection on suspend if the application doesn't keep
					// a handle to it anymore. We will fetch a fresh Connection if the
					// application accesses the ConnectionHolder again after resume,
					// assuming that it will participate in the same transaction.
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
					this.connectionHolder.setConnection(null);
				}
			}
		}

		@Override
		public void resume() {
			if (this.holderActive) {
				TransactionSynchronizationManager.bindResource(this.dataSource, this.connectionHolder);
			}
		}

		@Override
		public void beforeCompletion() {
			// Release Connection early if the holder is not open anymore
			// (that is, not used by another resource like a Hibernate Session
			// that has its own cleanup via transaction synchronization),
			// to avoid issues with strict JTA implementations that expect
			// the close call before transaction completion.
			if (!this.connectionHolder.isOpen()) {
				TransactionSynchronizationManager.unbindResource(this.dataSource);
				this.holderActive = false;
				if (this.connectionHolder.hasConnection()) {
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
				}
			}
		}

		@Override
		public void afterCompletion(int status) {
			// If we haven't closed the Connection in beforeCompletion,
			// close it now. The holder might have been used for other
			// cleanup in the meantime, for example by a Hibernate Session.
			if (this.holderActive) {
				// The thread-bound ConnectionHolder might not be available anymore,
				// since afterCompletion might get called from a different thread.
				TransactionSynchronizationManager.unbindResourceIfPossible(this.dataSource);
				this.holderActive = false;
				if (this.connectionHolder.hasConnection()) {
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
					// Reset the ConnectionHolder: It might remain bound to the thread.
					this.connectionHolder.setConnection(null);
				}
			}
			this.connectionHolder.reset();
		}
	}

}

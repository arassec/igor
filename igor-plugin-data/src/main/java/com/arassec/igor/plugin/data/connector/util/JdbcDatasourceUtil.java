package com.arassec.igor.plugin.data.connector.util;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.util.IgorException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Utility for JDBC datasource handling.
 * <p>
 * Since Spring helper classes like TransactionManager require a transaction to be started and committed / rolled back
 * in the same thread, and igor runs jobs multithreaded, this class uses "raw" JDBC connections.
 * <p>
 * Additionally, since igor components are Spring-Beans with scope "prototype", multiple instances of a
 * {@link com.arassec.igor.plugin.data.connector.JdbcDatasourceConnector} are created during job instantiation. The
 * database transactions have to span across multiple actions. Thus, different connector instances associated to different
 * actions require the same connections.
 * <p>
 * Hence, this helper bean with scope "singleton" keeps track of the database connections and transactions for the
 * connector instances.
 */
@Slf4j
@Component
public class JdbcDatasourceUtil {

    /**
     * Keeps track of data sources per job-execution ID.
     */
    @Getter
    private final Map<Long, Map<String, DriverManagerDataSource>> dataSources = new HashMap<>();

    /**
     * Keeps track of open connections per job-execution ID and transaction, identified by a UUID.
     */
    @Getter
    private final Map<Long, Map<UUID, Connection>> connections = new HashMap<>();

    /**
     * Initializes a datasource for the provided job-execution.
     * <p>
     * This method gets called every time a {@link com.arassec.igor.plugin.data.connector.JdbcDatasourceConnector}
     * instance is used in the job configuration!
     *
     * @param jobExecution          The current job-execution.
     * @param datasourceConnectorId The ID of the datasource connector to create the datasource for.
     * @param url                   The JDBC URL to use to connect to the database.
     * @param username              The database username.
     * @param password              The user's password.
     * @param driverClassName       The driver class name to use to connect to the database.
     */
    public synchronized void initializeDatasource(JobExecution jobExecution, String datasourceConnectorId, String url, String username, String password, String driverClassName) {
        dataSources.computeIfAbsent(jobExecution.getId(), jobExecutionId -> {
            connections.put(jobExecutionId, new HashMap<>());
            return new HashMap<>();
        });

        Map<String, DriverManagerDataSource> jobExecutionDataSources = dataSources.get(jobExecution.getId());

        jobExecutionDataSources.computeIfAbsent(datasourceConnectorId, s -> {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setDriverClassName(driverClassName);
            return dataSource;
        });
    }

    /**
     * Rolls back all open transactions of the provided job-execution.
     * <p>
     * This method gets called every time a {@link com.arassec.igor.plugin.data.connector.JdbcDatasourceConnector}
     * instance is used in the job configuration!
     *
     * @param jobExecution The current job-execution.
     */
    public synchronized void rollbackTransactions(JobExecution jobExecution) {
        if (connections.containsKey(jobExecution.getId())) {
            connections.get(jobExecution.getId()).forEach((uuid, connection) -> {
                try {
                    if (!connection.isClosed()) {
                        connection.rollback();
                        connection.close();
                    }
                } catch (SQLException e) {
                    throw new IgorException("Could not rollback or close connection for transaction ID: " + uuid, e);
                }
            });
            connections.remove(jobExecution.getId());
        }
    }

    /**
     * Cleans up open database connections for the provided job-execution.
     * <p>
     * This method gets called every time a {@link com.arassec.igor.plugin.data.connector.JdbcDatasourceConnector}
     * instance is used in the job configuration!
     *
     * @param jobExecution The current job-execution.
     */
    public synchronized void cleanupDatasource(JobExecution jobExecution) {
        if (connections.containsKey(jobExecution.getId())) {
            connections.get(jobExecution.getId()).forEach((uuid, connection) -> {
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    throw new IgorException("Could not close connection for transaction ID: " + uuid, e);
                }
            });
            connections.remove(jobExecution.getId());
        }
        dataSources.remove(jobExecution.getId());
    }

    /**
     * Starts a transaction under the provide transaction ID for the supplied job-execution.
     *
     * @param jobExecution          The current job-execution.
     * @param datasourceConnectorId The ID of the datasource connector to create the datasource for.
     * @param transactionId         The transaction ID to use to keep track of the transaction.
     */
    @SuppressWarnings("resource")
    public void startTransaction(JobExecution jobExecution, String datasourceConnectorId, UUID transactionId) {
        if (jobExecution == null || datasourceConnectorId == null || transactionId == null) {
            throw new IllegalArgumentException("JobExecution, datasourceConnectorId and transactionId must be set!");
        }
        Long jobExecutionId = jobExecution.getId();
        if (dataSources.containsKey(jobExecutionId) && dataSources.get(jobExecutionId).containsKey(datasourceConnectorId)) {
            connections.get(jobExecutionId).computeIfAbsent(transactionId, uuid -> {
                try {
                    Connection connection = dataSources.get(jobExecutionId).get(datasourceConnectorId).getConnection();
                    connection.setAutoCommit(false);
                    return connection;
                } catch (SQLException e) {
                    throw new IgorException("Could not connect to database!", e);
                }
            });
        }
    }

    /**
     * Commits the open transaction with the provided transaction ID.
     *
     * @param jobExecution  The current job-execution.
     * @param transactionId The ID of the transaction to commit.
     */
    @SuppressWarnings("resource")
    public void commitTransaction(JobExecution jobExecution, UUID transactionId) {
        if (jobExecution == null || transactionId == null) {
            throw new IllegalArgumentException("JobExecution and transactionId must be set!");
        }
        if (connections.containsKey(jobExecution.getId()) && connections.get(jobExecution.getId()).containsKey(transactionId)) {
            Connection connection = connections.get(jobExecution.getId()).get(transactionId);
            try {
                if (!connection.isClosed()) {
                    connection.commit();
                    connection.close();
                }
                connections.get(jobExecution.getId()).remove(transactionId);
            } catch (SQLException e) {
                throw new IgorException("Could not commit or close connection for transaction ID: " + transactionId, e);
            }
        }
    }

    /**
     * Queries a datasource with the provided SQL statement.
     *
     * @param jobExecution          The current-job execution.
     * @param datasourceConnectorId The ID of the datasource connector to create the datasource for.
     * @param transactionId         An optional transaction ID if a previously created transaction should be used for the query.
     *                              If {@code null}, the statement will be directly executed in a new transaction.
     * @param sql                   The SQL query to execute.
     * @return List of result sets. Each result set contains the column labels/names as key.
     */
    public List<Map<String, Object>> query(JobExecution jobExecution, String datasourceConnectorId, @Nullable UUID transactionId, String sql) {
        Connection connection = obtainConnection(jobExecution, datasourceConnectorId, transactionId);

        List<Map<String, Object>> result = new LinkedList<>();

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                int columnCount = resultSet.getMetaData().getColumnCount();
                if (columnCount > 0) {
                    Map<String, Object> resultEntry = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = resultSet.getMetaData().getColumnLabel(i);
                        Object columnValue = resultSet.getObject(i);
                        resultEntry.put(columnName, columnValue);
                    }
                    result.add(resultEntry);
                }
            }
        } catch (SQLException e) {
            throw new IgorException("Could not execute query!", e);
        }

        closeConnectionIfRequired(jobExecution, transactionId, connection);

        return result;
    }

    /**
     * Executes an SQL statement against the datasource.
     *
     * @param jobExecution          The current job-execution.
     * @param datasourceConnectorId The ID of the datasource connector to create the datasource for.
     * @param transactionId         An optional transaction ID, if the statement should be executed within a previously created
     *                              transaction. If {@code null}, the statement will be committed within a newly created transaction.
     * @param sql                   The SQL statement to execute.
     */
    public void execute(JobExecution jobExecution, String datasourceConnectorId, @Nullable UUID transactionId, String sql) {
        Connection connection = obtainConnection(jobExecution, datasourceConnectorId, transactionId);

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new IgorException("Could not execute statement!", e);
        }

        closeConnectionIfRequired(jobExecution, transactionId, connection);
    }

    /**
     * Either creates a new connection to the database or returns a previously created one, depending on a provided
     * transaction ID.
     *
     * @param jobExecution          The current job-execution.
     * @param datasourceConnectorId The ID of the datasource connector.
     * @param transactionId         An optional transaction ID.
     * @return A {@link Connection}.
     */
    private Connection obtainConnection(JobExecution jobExecution, String datasourceConnectorId, @Nullable UUID transactionId) {
        Connection connection;
        if (existingTransaction(jobExecution, transactionId)) {
            // Use the connection from an open transaction:
            connection = connections.get(jobExecution.getId()).get(transactionId);
        } else if (dataSources.containsKey(jobExecution.getId()) && dataSources.get(jobExecution.getId()).containsKey(datasourceConnectorId)) {
            try {
                connection = dataSources.get(jobExecution.getId()).get(datasourceConnectorId).getConnection();
            } catch (SQLException e) {
                throw new IgorException("Could not get connection from datasource!", e);
            }
        } else {
            throw new IgorException("No connection configured!");
        }
        return connection;
    }

    /**
     * Returns whether a transaction exists for the given parameters or not.
     *
     * @param jobExecution  The current job-execution.
     * @param transactionId An optional transaction ID.
     * @return {@code true} if a transaction for the given job-execution and transaction ID exists, {@code false} otherwise.
     */
    private boolean existingTransaction(JobExecution jobExecution, @Nullable UUID transactionId) {
        return (transactionId != null && connections.containsKey(jobExecution.getId()) && connections.get(jobExecution.getId()).containsKey(transactionId));
    }

    /**
     * Closes a connection if it should not be kept open in a transaction.
     *
     * @param jobExecution  The current job-execution.
     * @param transactionId An optional transaction ID.
     * @param connection    The connection to either close or keep open, if it is part of a running transaction.
     */
    private void closeConnectionIfRequired(JobExecution jobExecution, @Nullable UUID transactionId, Connection connection) {
        if (!existingTransaction(jobExecution, transactionId)) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new IgorException("Could not close open connection!", e);
            }
        }
    }
}

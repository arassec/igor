package com.arassec.igor.plugin.data.connector;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.connector.BaseConnector;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.data.util.JdbcDatasourceUtil;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <h2>JDBC-Datasource Connector</h2>
 *
 * <h3>Description</h3>
 * A connector for JDBC compatible databases.
 * <p>
 * The following JDBC drivers are included in igor:
 * <p>
 * <ul>
 *   <li>H2</li>
 *   <li>PostgreSQL</li>
 *   <li>Oracle</li>
 *   <li>IBM DB2</li>
 *   <li>Microsoft SQL Server</li>
 * </ul>
 * <p>
 * For additional databases, integrate the igor-spring-boot-starter into a new Spring-Boot project as described in the
 * documentation and add the respective JDBC JAR-file to the classpath.
 */
@Slf4j
@RequiredArgsConstructor
@IgorComponent(categoryId = CoreCategory.DATA, typeId = "jdbc-datasource-connector")
public class JdbcDatasourceConnector extends BaseConnector {

    /**
     * The URL to the datasource, e.g. 'jdbc:postgresql://localhost:5432/igor'.
     */
    @Getter
    @Setter
    @NotEmpty
    @IgorParam
    private String url;

    /**
     * The username to use to connect to the datasource.
     */
    @Getter
    @Setter
    @NotEmpty
    @IgorParam
    private String username;

    /**
     * The user's password.
     */
    @Getter
    @Setter
    @NotEmpty
    @IgorParam(secured = true)
    private String password;

    /**
     * The driver class name, e.g. 'org.postgresql.Driver'
     */
    @Getter
    @Setter
    @NotEmpty
    @IgorParam
    private String driverClassName;

    /**
     * The datasource store containing all DBMS related classes in a singleton Spring bean.
     */
    private final JdbcDatasourceUtil jdbcDatasourceUtil;

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() {
        try {
            DriverManagerDataSource testDataSource = new DriverManagerDataSource();
            testDataSource.setUrl(url);
            testDataSource.setUsername(username);
            testDataSource.setPassword(password);
            testDataSource.setDriverClassName(driverClassName);

            testDataSource.getConnection();
        } catch (Exception e) {
            throw new IgorException("Could not connect to JDBC datasource!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(JobExecution jobExecution) {
        super.initialize(jobExecution);
        jdbcDatasourceUtil.initializeDatasource(jobExecution, getId(), url, username, password, driverClassName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown(JobExecution jobExecution) {
        super.shutdown(jobExecution);

        if (JobExecutionState.FINISHED != jobExecution.getExecutionState()) {
            jdbcDatasourceUtil.rollbackTransactions(jobExecution);
        }

        jdbcDatasourceUtil.cleanupDatasource(jobExecution);
    }

    /**
     * Starts a transaction for the provided job execution and ID.
     *
     * @param jobExecution  The job execution the transaction is created for.
     * @param transactionId The provided ID to internally identify the transaction.
     */
    @IgorSimulationSafe
    public void startTransaction(JobExecution jobExecution, UUID transactionId) {
        if (jobExecution != null && transactionId != null) {
            jdbcDatasourceUtil.startTransaction(jobExecution, getId(), transactionId);
        }
    }

    /**
     * Queries a database with the provided SQL statement.
     *
     * @param jobExecution  The current job execution.
     * @param transactionId The optional transaction ID if available.
     * @param sql           The SQL query to execute.
     * @return List of result sets from the query or an empty list, if no data was selected.
     */
    @IgorSimulationSafe
    public List<Map<String, Object>> query(JobExecution jobExecution, @Nullable UUID transactionId, String sql) {
        return jdbcDatasourceUtil.query(jobExecution, getId(), transactionId, sql);
    }

    /**
     * Executes an SQL statement.
     *
     * @param jobExecution  The current job execution.
     * @param transactionId The optional transaction ID if available.
     * @param sql           The SQL statement to execute.
     */
    public void execute(JobExecution jobExecution, @Nullable UUID transactionId, String sql) {
        jdbcDatasourceUtil.execute(jobExecution, getId(), transactionId, sql);
    }

    /**
     * Commits the specified transaction.
     *
     * @param jobExecution  The current job execution.
     * @param transactionId The ID of the transaction to commit.
     */
    @IgorSimulationSafe
    public void commitTransaction(JobExecution jobExecution, UUID transactionId) {
        if (jobExecution != null && transactionId != null) {
            jdbcDatasourceUtil.commitTransaction(jobExecution, transactionId);
        }
    }

}

package com.arassec.igor.plugin.data.connector;


import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.data.util.JdbcDatasourceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JdbcDatasourceConnector}.
 */
@DisplayName("JDBC-Datasource-Connector tests.")
class JdbcDatasourceConnectorTest {

    /**
     * The connector under test.
     */
    private JdbcDatasourceConnector datasourceConnector;

    /**
     * Mock of the {@link JdbcDatasourceUtil}.
     */
    private JdbcDatasourceUtil datasourceUtilMock;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        datasourceUtilMock = mock(JdbcDatasourceUtil.class);

        datasourceConnector = new JdbcDatasourceConnector(datasourceUtilMock);
        datasourceConnector.setId("datasourceConnectorId");
        datasourceConnector.setUrl("jdbc:h2:mem:testdb");
        datasourceConnector.setDriverClassName("org.h2.Driver");
    }

    /**
     * Tests the connector's configuration test with an invalid configuration.
     */
    @Test
    @DisplayName("Tests the connector's configuration test with an invalid configuration.")
    void testTestConfigurationFailsafe() {
        datasourceConnector.setUrl(null);
        assertThrows(IgorException.class, datasourceConnector::testConfiguration);
    }

    /**
     * Tests the connector's configuration test with a valid configuration.
     */
    @Test
    @DisplayName("Tests the connector's configuration test with a valid configuration.")
    void testTestConfiguration() {
        datasourceConnector.testConfiguration();
    }

    /**
     * Tests initializing the connector.
     */
    @Test
    @DisplayName("Tests initializing the connector.")
    void testInitialize() {
        JobExecution jobExecution = JobExecution.builder().build();

        datasourceConnector.initialize(jobExecution);

        ArgumentCaptor<JobExecution> argCap = ArgumentCaptor.forClass(JobExecution.class);
        verify(datasourceUtilMock, times(1)).initializeDatasource(argCap.capture(), eq("datasourceConnectorId"),
            eq("jdbc:h2:mem:testdb"), isNull(), isNull(), eq("org.h2.Driver"));
        assertEquals(jobExecution, argCap.getValue());
    }

    /**
     * Tests shutting the connector down after a failed job execution.
     */
    @Test
    @DisplayName("Tests shutting the connector down after a failed job execution")
    void testShutdownJobExecutionFailed() {
        JobExecution jobExecution = JobExecution.builder().executionState(JobExecutionState.FAILED).build();

        datasourceConnector.shutdown(jobExecution);

        ArgumentCaptor<JobExecution> argCap = ArgumentCaptor.forClass(JobExecution.class);
        verify(datasourceUtilMock, times(1)).rollbackTransactions(argCap.capture());
        assertEquals(jobExecution, argCap.getValue());

        argCap = ArgumentCaptor.forClass(JobExecution.class);
        verify(datasourceUtilMock, times(1)).cleanupDatasource(argCap.capture());
        assertEquals(jobExecution, argCap.getValue());
    }

    /**
     * Tests shutting the connector down after a successful job execution.
     */
    @Test
    @DisplayName("Tests shutting the connector down after a successful job execution")
    void testShutdown() {
        JobExecution jobExecution = JobExecution.builder().executionState(JobExecutionState.FINISHED).build();

        datasourceConnector.shutdown(jobExecution);

        verify(datasourceUtilMock, times(0)).rollbackTransactions(any(JobExecution.class));

        ArgumentCaptor<JobExecution> argCap = ArgumentCaptor.forClass(JobExecution.class);
        verify(datasourceUtilMock, times(1)).cleanupDatasource(argCap.capture());
        assertEquals(jobExecution, argCap.getValue());
    }

    /**
     * Tests starting a transaction with insufficient input.
     */
    @Test
    @DisplayName("Tests starting a transaction with insufficient input.")
    void testStartTransactionFailsafe() {
        datasourceConnector.startTransaction(null, null);
        verify(datasourceUtilMock, times(0)).startTransaction(any(JobExecution.class), any(String.class), any(UUID.class));

        datasourceConnector.startTransaction(JobExecution.builder().build(), null);
        verify(datasourceUtilMock, times(0)).startTransaction(any(JobExecution.class), any(String.class), any(UUID.class));

        datasourceConnector.startTransaction(null, UUID.randomUUID());
        verify(datasourceUtilMock, times(0)).startTransaction(any(JobExecution.class), any(String.class), any(UUID.class));
    }

    /**
     * Tests starting a transaction.
     */
    @Test
    @DisplayName("Tests starting a transaction.")
    void testStartTransaction() {
        JobExecution jobExecution = JobExecution.builder().build();
        UUID transactionId = UUID.randomUUID();

        datasourceConnector.startTransaction(jobExecution, transactionId);

        verify(datasourceUtilMock, times(1)).startTransaction(jobExecution, datasourceConnector.getId(), transactionId);
    }

    /**
     * Tests querying a datasource.
     */
    @Test
    @DisplayName("Tests querying a datasource.")
    void testQuery() {
        JobExecution jobExecution = JobExecution.builder().build();
        UUID transactionId = UUID.randomUUID();

        datasourceConnector.query(jobExecution, null, "sql-query");

        verify(datasourceUtilMock, times(1)).query(jobExecution, datasourceConnector.getId(), null, "sql-query");

        datasourceConnector.query(jobExecution, transactionId, "sql-query-two");

        verify(datasourceUtilMock, times(1)).query(jobExecution, datasourceConnector.getId(), transactionId, "sql-query-two");
    }

    /**
     * Tests executing a statement.
     */
    @Test
    @DisplayName("Tests executing a statement.")
    void testExecute() {
        JobExecution jobExecution = JobExecution.builder().build();
        UUID transactionId = UUID.randomUUID();

        datasourceConnector.execute(jobExecution, null, "sql-statement");

        verify(datasourceUtilMock, times(1)).execute(jobExecution, datasourceConnector.getId(), null, "sql-statement");

        datasourceConnector.execute(jobExecution, transactionId, "sql-statement-two");

        verify(datasourceUtilMock, times(1)).execute(jobExecution, datasourceConnector.getId(), transactionId, "sql-statement-two");
    }

    /**
     * Tests committing a transaction with insufficient data.
     */
    @Test
    @DisplayName("Tests committing a transaction with insufficient data.")
    void testCommitTransactionFailsafe() {
        datasourceConnector.commitTransaction(null, null);
        verify(datasourceUtilMock, times(0)).commitTransaction(any(JobExecution.class), any(UUID.class));

        datasourceConnector.commitTransaction(JobExecution.builder().build(), null);
        verify(datasourceUtilMock, times(0)).commitTransaction(any(JobExecution.class), any(UUID.class));

        datasourceConnector.commitTransaction(null, UUID.randomUUID());
        verify(datasourceUtilMock, times(0)).commitTransaction(any(JobExecution.class), any(UUID.class));
    }

    /**
     * Tests committing a transaction.
     */
    @Test
    @DisplayName("Tests committing a transaction.")
    void testCommitTransaction() {
        JobExecution jobExecution = JobExecution.builder().build();
        UUID transactionId = UUID.randomUUID();

        datasourceConnector.commitTransaction(jobExecution, transactionId);
        verify(datasourceUtilMock, times(1)).commitTransaction(jobExecution, transactionId);
    }

}

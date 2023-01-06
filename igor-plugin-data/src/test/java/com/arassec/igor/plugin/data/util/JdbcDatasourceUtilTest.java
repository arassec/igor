package com.arassec.igor.plugin.data.util;

import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JdbcDatasourceUtil}.
 */
@DisplayName("JDBC-Datasource-Util tests.")
class JdbcDatasourceUtilTest {

    /**
     * A {@link JobExecution} for testing.
     */
    private final JobExecution jobExecution = JobExecution.builder().id(1L).build();

    /**
     * Tests initializing a datasource.
     */
    @Test
    @DisplayName("Tests initializing a datasource.")
    void testInitializeDatasource() {
        JdbcDatasourceUtil datasourceUtil = new JdbcDatasourceUtil();

        datasourceUtil.initializeDatasource(jobExecution, "datasourceConnectorId", "jdbc:h2:mem:testdb", null, null, "org.h2.Driver");

        assertNotNull(datasourceUtil.getDataSources().get(1L).get("datasourceConnectorId"));
    }

    /**
     * Tests rolling back a transaction.
     */
    @Test
    @DisplayName("Tests rolling back a transaction.")
    @SneakyThrows
    void testRollbackTransaction() {
        Connection connectionMock = mock(Connection.class);
        when(connectionMock.isClosed()).thenReturn(false);

        JdbcDatasourceUtil datasourceUtil = new JdbcDatasourceUtil();
        datasourceUtil.getConnections().put(1L, new HashMap<>());
        datasourceUtil.getConnections().get(1L).put(UUID.randomUUID(), connectionMock);

        datasourceUtil.rollbackTransactions(jobExecution);

        verify(connectionMock, times(1)).rollback();
        verify(connectionMock, times(1)).close();

        assertTrue(datasourceUtil.getConnections().isEmpty());
    }

    /**
     * Tests cleanup of a datasource.
     */
    @Test
    @DisplayName("Tests cleanup of a datasource.")
    @SneakyThrows
    void testCleanup() {
        Connection connectionMock = mock(Connection.class);
        when(connectionMock.isClosed()).thenReturn(false);

        JdbcDatasourceUtil datasourceUtil = new JdbcDatasourceUtil();
        datasourceUtil.getConnections().put(1L, new HashMap<>());
        datasourceUtil.getConnections().get(1L).put(UUID.randomUUID(), connectionMock);
        datasourceUtil.getDataSources().put(1L, new HashMap<>());

        datasourceUtil.cleanupDatasource(jobExecution);

        verify(connectionMock, times(1)).close();
        assertTrue(datasourceUtil.getConnections().isEmpty());

        assertTrue(datasourceUtil.getDataSources().isEmpty());
    }

    /**
     * Tests starting a transaction without parameters to fail-safe.
     */
    @Test
    @DisplayName("Tests starting a transaction without parameters to fail safe.")
    void testStartTransactionFailsafe() {
        JdbcDatasourceUtil datasourceUtil = new JdbcDatasourceUtil();

        UUID transactionId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> datasourceUtil.startTransaction(null, null, null));
        assertThrows(IllegalArgumentException.class, () -> datasourceUtil.startTransaction(jobExecution, null, null));
        assertThrows(IllegalArgumentException.class, () -> datasourceUtil.startTransaction(jobExecution, "connectorId", null));
        assertThrows(IllegalArgumentException.class, () -> datasourceUtil.startTransaction(jobExecution, null, transactionId));
        assertThrows(IllegalArgumentException.class, () -> datasourceUtil.startTransaction(null, "connectorId", transactionId));
    }

    /**
     * Tests starting a transaction.
     */
    @Test
    @DisplayName("Tests starting a transaction.")
    void testStartTransaction() {
        JdbcDatasourceUtil datasourceUtil = new JdbcDatasourceUtil();
        datasourceUtil.initializeDatasource(jobExecution, "datasourceConnectorId", "jdbc:h2:mem:testdb", null, null, "org.h2.Driver");

        UUID transactionId = UUID.randomUUID();

        datasourceUtil.startTransaction(jobExecution, "datasourceConnectorId", transactionId);

        assertNotNull(datasourceUtil.getConnections().get(1L).get(transactionId));
    }

    /**
     * Tests committing a transaction with invalid input parameters.
     */
    @Test
    @DisplayName("Tests committing a transaction with invalid input parameters.")
    void testCommitTransactionFailsafe() {
        JdbcDatasourceUtil datasourceUtil = new JdbcDatasourceUtil();
        UUID transactionId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> datasourceUtil.commitTransaction(null, null));
        assertThrows(IllegalArgumentException.class, () -> datasourceUtil.commitTransaction(jobExecution, null));
        assertThrows(IllegalArgumentException.class, () -> datasourceUtil.commitTransaction(null, transactionId));
    }

    /**
     * Tests committing a transaction.
     */
    @Test
    @DisplayName("Tests committing a transaction.")
    void testCommitTransaction() {
        UUID transactionId = UUID.randomUUID();

        JdbcDatasourceUtil datasourceUtil = new JdbcDatasourceUtil();
        datasourceUtil.initializeDatasource(jobExecution, "datasourceConnectorId", "jdbc:h2:mem:testdb", null, null, "org.h2.Driver");
        datasourceUtil.startTransaction(jobExecution, "datasourceConnectorId", transactionId);

        assertNotNull(datasourceUtil.getConnections().get(1L).get(transactionId));

        datasourceUtil.commitTransaction(jobExecution, transactionId);

        assertNull(datasourceUtil.getConnections().get(1L).get(transactionId));
    }

    /**
     * Tests querying data from within an existing transaction.
     */
    @SuppressWarnings("SqlDialectInspection")
    @Test
    @DisplayName("Tests querying data from within an existing transaction.")
    @SneakyThrows
    void testQueryWithinTransaction() {
        UUID transactionId = UUID.randomUUID();

        ResultSetMetaData resultSetMetaDataMock = mock(ResultSetMetaData.class);
        when(resultSetMetaDataMock.getColumnCount()).thenReturn(2);
        when(resultSetMetaDataMock.getColumnLabel(1)).thenReturn("columnOne");
        when(resultSetMetaDataMock.getColumnLabel(2)).thenReturn("columnTwo");

        ResultSet resultSetMock = mock(ResultSet.class);
        when(resultSetMock.getMetaData()).thenReturn(resultSetMetaDataMock);
        when(resultSetMock.getObject(1)).thenReturn("valueOne");
        when(resultSetMock.getObject(2)).thenReturn("valueTwo");
        when(resultSetMock.next()).thenAnswer(new Answer<Boolean>() {

            private int invocations = 0;

            @Override
            public Boolean answer(InvocationOnMock invocationOnMock) {
                invocations++;
                return invocations <= 1;
            }
        });

        Statement statementMock = mock(Statement.class);
        when(statementMock.executeQuery("query")).thenReturn(resultSetMock);

        Connection connectionMock = mock(Connection.class);
        when(connectionMock.createStatement()).thenReturn(statementMock);

        JdbcDatasourceUtil datasourceUtil = new JdbcDatasourceUtil();
        datasourceUtil.getConnections().put(1L, new HashMap<>());
        datasourceUtil.getConnections().get(1L).put(transactionId, connectionMock);

        List<Map<String, Object>> result = datasourceUtil.query(jobExecution, "datasourceConnectorId", transactionId, "query");

        assertEquals(1, result.size());
        assertEquals("valueOne", result.get(0).get("columnOne"));
        assertEquals("valueTwo", result.get(0).get("columnTwo"));
    }

    /**
     * Tests executing a statement within a newly created transaction.
     */
    @SuppressWarnings("SqlDialectInspection")
    @Test
    @DisplayName("Tests executing a statement within a newly created transaction.")
    @SneakyThrows
    void testExecute() {
        Statement statementMock = mock(Statement.class);

        Connection connectionMock = mock(Connection.class);
        when(connectionMock.createStatement()).thenReturn(statementMock);

        DriverManagerDataSource datasourceMock = mock(DriverManagerDataSource.class);
        when(datasourceMock.getConnection()).thenReturn(connectionMock);

        JdbcDatasourceUtil datasourceUtil = new JdbcDatasourceUtil();
        datasourceUtil.getDataSources().put(1L, new HashMap<>());
        datasourceUtil.getDataSources().get(1L).put("datasourceConnectorId", datasourceMock);

        datasourceUtil.execute(jobExecution, "datasourceConnectorId", null, "statement");

        verify(statementMock, times(1)).execute("statement");
    }

}

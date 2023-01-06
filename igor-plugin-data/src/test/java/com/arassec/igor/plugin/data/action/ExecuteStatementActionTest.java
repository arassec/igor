package com.arassec.igor.plugin.data.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.data.connector.JdbcDatasourceConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Tests the {@link ExecuteStatementAction}.
 */
@DisplayName("'Execute Statement' action tests.")
class ExecuteStatementActionTest {

    /**
     * The action under test.
     */
    private ExecuteStatementAction action;

    /**
     * A mock for the {@link JdbcDatasourceConnector}.
     */
    private JdbcDatasourceConnector connectorMock;

    /**
     * The data item to process.
     */
    private Map<String, Object> data;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        connectorMock = mock(JdbcDatasourceConnector.class);

        action = new ExecuteStatementAction();
        action.setDatasourceConnector(connectorMock);
        action.setSqlStatement("{{sqlStatement}}");
        action.setTransactionKey("{{transactionId}}");

        data = new HashMap<>();
        data.put("sqlStatement", "sql-statement");
        data.put("transactionId", "transaction-id");
    }

    /**
     * Tests executing a statement without existing transaction.
     */
    @Test
    @DisplayName("Tests executing a statement without existing transaction.")
    void testProcessWithoutTransaction() {
        action.process(data, JobExecution.builder().build());

        verify(connectorMock, times(1)).execute(any(JobExecution.class), isNull(), eq("sql-statement"));
    }

    /**
     * Tests executing a statement with an existing transaction.
     */
    @Test
    @DisplayName("Tests executing a statement with an existing transaction.")
    void testProcessWithTransaction() {
        UUID transactionId = UUID.randomUUID();

        data.put("transaction-id", transactionId);

        action.process(data, JobExecution.builder().build());

        verify(connectorMock, times(1)).execute(any(JobExecution.class), eq(transactionId), eq("sql-statement"));
    }

}

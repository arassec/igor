package com.arassec.igor.plugin.data.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.data.connector.JdbcDatasourceConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link QueryDataAction}.
 */
@DisplayName("'Query Data' action tests.")
class QueryDataActionTest {

    /**
     * The action under test.
     */
    private QueryDataAction action;

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

        action = new QueryDataAction();
        action.setDatasourceConnector(connectorMock);
        action.setSqlStatement("{{sqlStatement}}");
        action.setTransactionKey("{{transactionId}}");
        action.setTargetKey("{{result}}");

        data = new HashMap<>();
        data.put("sqlStatement", "sql-statement");
        data.put("transactionId", "transaction-id");
        data.put("result", "query-result");
    }

    /**
     * Tests querying data without an existing transaction.
     */
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Tests querying data without an existing transaction.")
    void testProcessWithoutTransaction() {
        when(connectorMock.query(any(JobExecution.class), isNull(), eq("sql-statement")))
            .thenReturn(List.of(
                Map.of("column", "value-one"),
                Map.of("column", "value-two")
            ));

        List<Map<String, Object>> result = action.process(data, JobExecution.builder().build());
        assertEquals(1, result.size());

        List<Map<String, Object>> queryResult = (List<Map<String, Object>>) result.getFirst().get("query-result");
        assertEquals(2, queryResult.size());
        assertEquals("value-one", queryResult.getFirst().get("column"));
        assertEquals("value-two", queryResult.get(1).get("column"));
    }

    /**
     * Tests querying data with an existing transaction.
     */
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Tests querying data with an existing transaction.")
    void testProcessWithTransaction() {
        UUID transactionId = UUID.randomUUID();

        when(connectorMock.query(any(JobExecution.class), eq(transactionId), eq("sql-statement")))
            .thenReturn(List.of(
                Map.of("column", "value")
            ));

        data.put("transaction-id", transactionId);

        action.process(data, JobExecution.builder().build());

        List<Map<String, Object>> result = action.process(data, JobExecution.builder().build());
        assertEquals(1, result.size());

        List<Map<String, Object>> queryResult = (List<Map<String, Object>>) result.getFirst().get("query-result");
        assertEquals(1, queryResult.size());
        assertEquals("value", queryResult.getFirst().get("column"));
    }

}

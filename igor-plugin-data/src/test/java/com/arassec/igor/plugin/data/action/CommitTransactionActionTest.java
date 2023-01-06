package com.arassec.igor.plugin.data.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.data.connector.JdbcDatasourceConnector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Commit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link CommitTransactionAction}.
 */
@DisplayName("'Commit Transaction' action tests.")
class CommitTransactionActionTest {

    /**
     * Tests that trying to commit a non-existing transaction does not fail.
     */
    @Test
    @DisplayName("Tests that trying to commit a non-existing transaction does not fail.")
    void testProcessFailsafe() {
        JdbcDatasourceConnector connectorMock = mock(JdbcDatasourceConnector.class);

        CommitTransactionAction action = new CommitTransactionAction();
        action.setDatasourceConnector(connectorMock);
        action.setTransactionKey("transactionId");

        // Must not throw any exception:
        action.process(new HashMap<>(), JobExecution.builder().build());
    }

    /**
     * Tests commiting a transaction.
     */
    @Test
    @DisplayName("Tests commiting a transaction.")
    void testProcess() {
        JdbcDatasourceConnector connectorMock = mock(JdbcDatasourceConnector.class);

        CommitTransactionAction action = new CommitTransactionAction();
        action.setDatasourceConnector(connectorMock);
        action.setTransactionKey("transactionId");

        UUID transactionId = UUID.randomUUID();

        Map<String, Object> data = new HashMap<>();
        data.put("transactionId", transactionId);

        // Must not throw any exception:
        action.process(data, JobExecution.builder().build());

        verify(connectorMock, times(1)).commitTransaction(any(JobExecution.class), eq(transactionId));

        assertFalse(data.containsKey("transactionId"));
    }



}

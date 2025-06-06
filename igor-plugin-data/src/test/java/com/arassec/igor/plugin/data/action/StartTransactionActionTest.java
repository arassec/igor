package com.arassec.igor.plugin.data.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.data.connector.JdbcDatasourceConnector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link StartTransactionAction}.
 */
@DisplayName("'Start Transaction' action tests.")
class StartTransactionActionTest {

    /**
     * Tests that open transactions prevent creating new ones.
     */
    @Test
    @DisplayName("Tests that open transactions prevent creating new ones.")
    void testProcessFailOnExistingTransaction() {
        Map<String, Object> data = new HashMap<>();
        data.put("transactionKey", "tx-id");
        data.put("tx-id", UUID.randomUUID());

        StartTransactionAction action = new StartTransactionAction();
        action.setTransactionKey("{{transactionKey}}");

        JobExecution jobExecution = JobExecution.builder().build();

        assertThrows(IgorException.class, () -> action.process(data, jobExecution));
    }

    /**
     * Tests creating a transaction.
     */
    @Test
    @DisplayName("Tests creating a transaction.")
    void testProcess() {
        JdbcDatasourceConnector connectorMock = mock(JdbcDatasourceConnector.class);

        StartTransactionAction action = new StartTransactionAction();
        action.setDatasourceConnector(connectorMock);
        action.setTransactionKey("tx-id");

        List<Map<String, Object>> result = action.process(new HashMap<>(), JobExecution.builder().build());
        assertEquals(1, result.size());

        ArgumentCaptor<UUID> argCap = ArgumentCaptor.forClass(UUID.class);

        verify(connectorMock, times(1)).startTransaction(any(JobExecution.class), argCap.capture());

        assertEquals(argCap.getValue(), result.getFirst().get("tx-id"));
    }

}

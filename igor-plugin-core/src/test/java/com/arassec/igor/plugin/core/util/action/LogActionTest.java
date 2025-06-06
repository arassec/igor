package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreActionBaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link LogAction}.
 */
@DisplayName("'Log action' tests.")
class LogActionTest extends CoreActionBaseTest {

    /**
     * Tests action initialization.
     */
    @Test
    @DisplayName("Tests action initialization.")
    void testInitialize() {
        LogAction logAction = new LogAction();
        logAction.initialize(JobExecution.builder().jobId("job-id").build());
        assertEquals("job-id", logAction.getJobId());
    }

    /**
     * Tests that the action always returns the input for further processing.
     */
    @Test
    @DisplayName("Tests that the action always returns the input for further processing.")
    void testProcessResult() {
        LogAction logAction = new LogAction();
        Map<String, Object> data = createData();

        List<Map<String, Object>> result = logAction.process(data, new JobExecution());

        assertEquals(1, result.size());
        assertEquals(data, result.getFirst());
    }

    /**
     * Tests adding a log message to simulation job data.
     */
    @Test
    @DisplayName("Tests adding a log message to simulation job data.")
    @SuppressWarnings("unchecked")
    void testProcessSimulated() {
        LogAction logAction = new LogAction();
        Map<String, Object> data = createData();
        ((Map<String, Object>) data.get(DataKey.META.getKey())).put(DataKey.SIMULATION.getKey(), true);

        List<Map<String, Object>> result = logAction.process(data, new JobExecution());

        assertEquals(1, result.size());
        assertNotNull(result.getFirst().get(DataKey.SIMULATION_LOG.getKey()));
    }

    /**
     * Tests logging messages in different log levels.
     */
    @Test
    @DisplayName("Tests logging messages in different log levels.")
    void testLogLevels() {
        LogAction logAction = new LogAction();
        Map<String, Object> data = createData();
        data.put("logTest", "loggable value");

        logAction.setLevel("error");
        logAction.setMessage("Test error logging: '{{logTest}}'");
        assertDoesNotThrow(() -> logAction.process(data, new JobExecution()));

        logAction.setLevel("warn");
        logAction.setMessage("Test warn logging: '{{logTest}}'");
        assertDoesNotThrow(() -> logAction.process(data, new JobExecution()));

        logAction.setLevel("info");
        logAction.setMessage("Test info logging: '{{logTest}}'");
        assertDoesNotThrow(() -> logAction.process(data, new JobExecution()));

        logAction.setLevel("debug");
        logAction.setMessage("Test debug logging: '{{logTest}}'");
        assertDoesNotThrow(() -> logAction.process(data, new JobExecution()));

        logAction.setLevel("trace");
        logAction.setMessage("Test trace logging: '{{logTest}}'");
        assertDoesNotThrow(() -> logAction.process(data, new JobExecution()));
    }

}

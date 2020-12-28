package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreActionBaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        logAction.initialize("job-id", new JobExecution());
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
        assertEquals(data, result.get(0));
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
        assertNotNull(result.get(0).get(DataKey.SIMULATION_LOG.getKey()));
    }

}
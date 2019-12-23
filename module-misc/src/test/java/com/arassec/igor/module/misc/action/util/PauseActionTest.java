package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.misc.action.BaseMiscActionTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link PauseAction}.
 */
@DisplayName("'Pause action' tests.")
class PauseActionTest extends BaseMiscActionTest {

    /**
     * Tests pausing the job run during simulations.
     */
    @Test
    @DisplayName("Tests the pausing functionality during simulated job runs.")
    @SuppressWarnings("unchecked")
    void testProcessDuringSimulation() {
        PauseAction pauseAction = new PauseAction();
        pauseAction.setMilliseconds(666);

        Map<String, Object> data = createData();
        ((Map<String, Object>) data.get(DataKey.DATA.getKey())).put(DataKey.SIMULATION.getKey(), true);

        List<Map<String, Object>> processedData = pauseAction.process(data, new JobExecution());
        assertEquals("Would have paused for 666 milliseconds.", processedData.get(0).get(DataKey.SIMULATION_LOG.getKey()));
    }

    /**
     * Tests pausing the job run during normal execution.
     */
    @Test
    @DisplayName("Tests running the action during normal job runs.")
    void testProcess() {
        PauseAction pauseAction = new PauseAction();
        pauseAction.setMilliseconds(23);

        Map<String, Object> data = createData();

        List<Map<String, Object>> processedData = pauseAction.process(data, new JobExecution());

        assertEquals(data, processedData.get(0));
    }

}
package com.arassec.igor.plugin.core.util.action;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreActionBaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link PauseAction}.
 */
@DisplayName("'Pause action' tests.")
class PauseActionTest extends CoreActionBaseTest {

    /**
     * Tests pausing the job run during simulations.
     */
    @Test
    @DisplayName("Tests the pausing functionality during simulated job runs.")
    @SuppressWarnings("unchecked")
    void testProcessDuringSimulation() {
        PauseAction pauseAction = new PauseAction();
        pauseAction.setMilliseconds(666L);

        Map<String, Object> data = createData();
        ((Map<String, Object>) data.get(DataKey.META.getKey())).put(DataKey.SIMULATION.getKey(), true);

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
        pauseAction.setMilliseconds(23L);

        Map<String, Object> data = createData();

        List<Map<String, Object>> processedData = pauseAction.process(data, new JobExecution());

        assertEquals(data, processedData.get(0));
    }

    /**
     * Tests interrupting the action during pause.
     */
    @Test
    @DisplayName("Tests interrupting the action.")
    void testInterruption() {
        PauseAction pauseAction = new PauseAction();
        pauseAction.setMilliseconds(5000L);

        Logger logger = (Logger) LoggerFactory.getLogger(PauseAction.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        Thread.currentThread().interrupt();
        pauseAction.process(createData(), new JobExecution());

        assertEquals("Interrupted during pause action!", listAppender.list.get(0).getMessage());
    }

}
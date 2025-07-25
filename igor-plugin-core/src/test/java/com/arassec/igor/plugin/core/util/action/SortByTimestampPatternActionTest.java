package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreActionBaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link SortByTimestampPatternAction}.
 */
@DisplayName("'Sort by timestamp pattern' tests.")
class SortByTimestampPatternActionTest extends CoreActionBaseTest {

    /**
     * Tests the action's configuration.
     */
    @Test
    @DisplayName("Tests the action's configuration.")
    void testConfiguration() {
        SortByTimestampPatternAction action = new SortByTimestampPatternAction();
        assertTrue(action.enforceSingleThread());
    }

    /**
     * Tests processing data.
     */
    @Test
    @DisplayName("Tests that processing only collects the data.")
    void testProcess() {
        SortByTimestampPatternAction action = new SortByTimestampPatternAction();
        Map<String, Object> data = createData();
        List<Map<String, Object>> processedData = action.process(data, new JobExecution());
        assertTrue(processedData.isEmpty());
        assertEquals(data, action.getCollectedData().getFirst());
        processedData = action.process(createData(), new JobExecution());
        assertTrue(processedData.isEmpty());
        assertEquals(2, action.getCollectedData().size());
    }

    /**
     * Tests completing the action.
     */
    @Test
    @DisplayName("Tests sorting the collected data.")
    void testComplete() {
        SortByTimestampPatternAction action = new SortByTimestampPatternAction();
        action.setInput("{{timestamp}}");
        action.setPattern(".*");
        action.setTimestampFormat("yyyy-MM-dd'T'HH:mm:ss");

        action.getCollectedData().add(Map.of("timestamp", "2019-12-29T14:13:12"));
        action.getCollectedData().add(Map.of("timestamp", "2014-04-13T03:00:00"));
        action.getCollectedData().add(Map.of("timestamp", "2019-12-29T14:13:11"));

        action.setSortAscending(true);

        List<Map<String, Object>> completedData = action.complete();
        assertEquals(3, completedData.size());
        assertEquals("2014-04-13T03:00:00", completedData.getFirst().get("timestamp"));
        assertEquals("2019-12-29T14:13:11", completedData.get(1).get("timestamp"));
        assertEquals("2019-12-29T14:13:12", completedData.get(2).get("timestamp"));

        action.setSortAscending(false);

        completedData = action.complete();
        assertEquals(3, completedData.size());
        assertEquals("2019-12-29T14:13:12", completedData.getFirst().get("timestamp"));
        assertEquals("2019-12-29T14:13:11", completedData.get(1).get("timestamp"));
        assertEquals("2014-04-13T03:00:00", completedData.get(2).get("timestamp"));

        action.getCollectedData().clear();
        assertTrue(action.complete().isEmpty());
    }

    /**
     * Tests sorting by timestamp with a pattern.
     */
    @Test
    @DisplayName("Tests sorting by timestamp with a pattern.")
    void testCompleteWithPattern() {
        SortByTimestampPatternAction action = new SortByTimestampPatternAction();
        action.setInput("{{filename}}");
        action.setPattern("{{pattern}}");
        action.setTimestampFormat("{{timestampFormat}}");
        action.setSortAscending(true);

        action.getCollectedData().add(
                Map.of("filename", "alpha_20200113185100_beta.jpeg",
                        "pattern", "[0-9]{14}",
                        "timestampFormat", "yyyyMMddHHmmss"));
        action.getCollectedData().add(
                Map.of("filename", "20191229185100-gamma.jpeg",
                        "pattern", "[0-9]{14}",
                        "timestampFormat", "yyyyMMddHHmmss"));

        List<Map<String, Object>> completedData = action.complete();
        assertEquals(2, completedData.size());
        assertEquals("20191229185100-gamma.jpeg", completedData.getFirst().get("filename"));
        assertEquals("alpha_20200113185100_beta.jpeg", completedData.get(1).get("filename"));
    }

    /**
     * Tests that the action doesn't support events.
     */
    @Test
    @DisplayName("Tests that the action doesn't support events.")
    void testSupportsEvents() {
        SortByTimestampPatternAction action = new SortByTimestampPatternAction();
        assertFalse(action.supportsEvents());
    }

}

package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.misc.action.BaseMiscActionTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link SortByTimestampPatternAction}.
 */
@DisplayName("'Sort by timestamp pattern' tests.")
class SortByTimestampPatternActionTest extends BaseMiscActionTest {

    /**
     * Tests the action's configuration.
     */
    @Test
    @DisplayName("Tests the action's configuration.")
    void testConfiguration() {
        SortByTimestampPatternAction action = new SortByTimestampPatternAction();
        assertEquals(1, action.getUnEditableProperties().size());
        assertEquals("numThreads", action.getUnEditableProperties().iterator().next());
        assertEquals(1, action.getNumThreads());
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
        assertEquals(data, action.getCollectedData().get(0));
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
        action.setInput("$.timestamp");
        action.setPattern(".*");
        action.setTimestampFormat("yyyy-MM-dd'T'HH:mm:ss");

        action.getCollectedData().add(Map.of("timestamp", "2019-12-29T14:13:12"));
        action.getCollectedData().add(Map.of("timestamp", "2014-04-13T03:00:00"));
        action.getCollectedData().add(Map.of("INVALID", "INVALID"));
        action.getCollectedData().add(Map.of("timestamp", "2019-12-29T14:13:11"));

        List<Map<String, Object>> completedData = action.complete();
        assertEquals(3, completedData.size());
        assertEquals("2014-04-13T03:00:00", completedData.get(0).get("timestamp"));
        assertEquals("2019-12-29T14:13:11", completedData.get(1).get("timestamp"));
        assertEquals("2019-12-29T14:13:12", completedData.get(2).get("timestamp"));

        action.setSortAscending(false);
        completedData = action.complete();
        assertEquals(3, completedData.size());
        assertEquals("2019-12-29T14:13:12", completedData.get(0).get("timestamp"));
        assertEquals("2019-12-29T14:13:11", completedData.get(1).get("timestamp"));
        assertEquals("2014-04-13T03:00:00", completedData.get(2).get("timestamp"));

        action.getCollectedData().clear();
        assertTrue(action.complete().isEmpty());
    }

    /**
     * Tests sorting by timestamp with timezone.
     */
    @Test
    @DisplayName("Tests sorting with timezone.")
    void testCompleteWithTimezone() {
        SortByTimestampPatternAction action = new SortByTimestampPatternAction();
        action.setInput("$.timestamp");
        action.setPattern("$.pattern");
        action.setTimestampFormat("$.timestampFormat");

        action.getCollectedData().add(
                Map.of("timestamp", "2019-12-29T18:51:00-02:00",
                        "pattern", ".*",
                        "timestampFormat", "yyyy-MM-dd'T'HH:mm:ssXXX"));
        action.getCollectedData().add(
                Map.of("timestamp", "2019-12-29T18:51:00+01:00",
                        "pattern", ".*",
                        "timestampFormat", "yyyy-MM-dd'T'HH:mm:ssXXX"));

        List<Map<String, Object>> completedData = action.complete();
        assertEquals(2, completedData.size());
        assertEquals("2019-12-29T18:51:00+01:00", completedData.get(0).get("timestamp"));
        assertEquals("2019-12-29T18:51:00-02:00", completedData.get(1).get("timestamp"));
    }

}
package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link SplitArrayAction}.
 */
@DisplayName("'Split Array action' tests.")
class SplitArrayActionTest {

    /**
     * Tests splitting an JSON-Array into multiple data items.
     */
    @Test
    @DisplayName("Tests splitting an JSON-Array into multiple data items.")
    @SuppressWarnings("unchecked")
    void testProcess() {
        List<String> elements = List.of("a", "b", "c");
        Map<String, Object> content = new HashMap<>(Map.of("content", elements));

        Map<String, Object> data = Map.of("data", content);

        SplitArrayAction splitArrayAction = new SplitArrayAction();
        splitArrayAction.setArraySelector("{{data.content}}");

        List<Map<String, Object>> result = splitArrayAction.process(data, new JobExecution());

        assertEquals(3, result.size());

        assertEquals("a", ((Map<String, Object>) result.get(0).get("data")).get("content"));
        assertEquals("b", ((Map<String, Object>) result.get(1).get("data")).get("content"));
        assertEquals("c", ((Map<String, Object>) result.get(2).get("data")).get("content"));
    }

    /**
     * Tests splitting a complex JSON-Array into multiple data items.
     */
    @Test
    @DisplayName("Tests splitting a complex JSON-Array into multiple data items.")
    @SuppressWarnings("unchecked")
    void testProcessComplex() {
        List<Map<String, Object>> elements = List.of(Map.of("a", "b"), Map.of("c", "d"));
        Map<String, Object> data = Map.of("data", elements);

        SplitArrayAction splitArrayAction = new SplitArrayAction();
        splitArrayAction.setArraySelector("{{data}}");

        List<Map<String, Object>> result = splitArrayAction.process(data, new JobExecution());

        assertEquals(2, result.size());

        assertEquals("b", ((Map<String, Object>) result.get(0).get("data")).get("a"));
        assertEquals("d", ((Map<String, Object>) result.get(1).get("data")).get("c"));
    }

    /**
     * Tests splitting invalid input.
     */
    @Test
    @DisplayName("Tests splitting invalid input.")
    void testProcessInvalidInput() {
        Map<String, Object> content = new HashMap<>(Map.of("content", "invalid"));

        Map<String, Object> data = Map.of("data", content);

        SplitArrayAction splitArrayAction = new SplitArrayAction();
        splitArrayAction.setArraySelector("{{data.content}}");

        List<Map<String, Object>> result = splitArrayAction.process(data, new JobExecution());

        assertTrue(result.isEmpty());
    }

}

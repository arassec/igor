package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link SplitStringAction}.
 */
@DisplayName("'Split String action' tests.")
class SplitStringActionTest {

    /**
     * Tests splitting a string.
     */
    @Test
    @DisplayName("Tests splitting a string.")
    @SuppressWarnings("unchecked")
    void testProcess() {
        Map<String, Object> data = new HashMap<>();

        SplitStringAction action = new SplitStringAction();
        action.setInput("a.simple.example.string");
        action.setRegex("\\.");
        action.setTargetKey("splitted");

        List<Map<String, Object>> result = action.process(data, JobExecution.builder().build());

        assertEquals(1, result.size());

        List<String> splitted = ((List<String>) result.get(0).get("splitted"));

        assertEquals("a", splitted.get(0));
        assertEquals("simple", splitted.get(1));
        assertEquals("example", splitted.get(2));
        assertEquals("string", splitted.get(3));
    }

}

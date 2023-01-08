package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CorePluginUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link XmlToJsonAction}.
 */
@DisplayName("'XML to JSON action' tests.")
class XmlToJsonActionTest {

    /**
     * Tests converting XML to JSON.
     */
    @Test
    @DisplayName("Tests converting XML to JSON.")
    @SuppressWarnings("unchecked")
    void testProcess() {
        Map<String, Object> data = new HashMap<>();

        XmlToJsonAction action = new XmlToJsonAction();
        action.setXml("""
            <?xml version="1.0" encoding="UTF-8"?>
            <element>
                <status>400</status>
                <message>This is an example.</message>
                <error>A</error>
                <error>B</error>
                <error>C</error>
            </element>
            """);

        List<Map<String, Object>> result = action.process(data, JobExecution.builder().build());

        assertEquals(1, result.size());

        Map<String, Object> json = result.get(0);

        Map<String, Object> convertedXml = ((Map<String, Object>) CorePluginUtils.getValue(json, "data.convertedXml", Map.class).orElseThrow());

        assertEquals("400", convertedXml.get("status"));
        assertEquals("This is an example.", convertedXml.get("message"));
        assertEquals("A", ((List<String>) convertedXml.get("error")).get(0));
        assertEquals("B", ((List<String>) convertedXml.get("error")).get(1));
        assertEquals("C", ((List<String>) convertedXml.get("error")).get(2));
    }

}

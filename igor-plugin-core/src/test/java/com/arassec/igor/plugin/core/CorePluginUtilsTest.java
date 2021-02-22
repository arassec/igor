package com.arassec.igor.plugin.core;


import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.plugin.core.util.trigger.ManualTrigger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link CorePluginUtils}.
 */
@DisplayName("'Common Utils' tests.")
class CorePluginUtilsTest {

    /**
     * The data for testing.
     */
    private static Map<String, Object> testData;

    /**
     * Initializes the test environment.
     */
    @SuppressWarnings("unchecked")
    @BeforeAll
    static void initialize() {
        Trigger trigger = new ManualTrigger();
        trigger.initialize(JobExecution.builder().jobId("job-id").build());

        testData = trigger.createDataItem();
        ((Map<String, Object>) testData.get(DataKey.META.getKey())).put(DataKey.SIMULATION.getKey(), true);
        testData.put(DataKey.TIMESTAMP.getKey(), 1122334455);
    }

    /**
     * Tests appending an optional suffix to a file name.
     */
    @Test
    @DisplayName("Tests appending an optional suffix to a file name.")
    void testAppendSuffixIfRequired() {
        assertEquals("file.txt", CorePluginUtils.appendSuffixIfRequired("file.txt", null, false));
        assertEquals("file", CorePluginUtils.appendSuffixIfRequired("file", ".igor", false));
        assertEquals("file.igor", CorePluginUtils.appendSuffixIfRequired("file", "igor", true));
        assertEquals("file.igor", CorePluginUtils.appendSuffixIfRequired("file", ".igor", true));
    }

    /**
     * Tests combining a path and a filename to an absolute path.
     */
    @Test
    @DisplayName("Tests combining a path and a filename to an absolute path.")
    void testCombineFilePath() {
        assertEquals("/file.txt", CorePluginUtils.combineFilePath(null, "file.txt"));
        assertEquals("directory/file.txt", CorePluginUtils.combineFilePath("directory", "file.txt"));
        assertEquals("directory/file.txt", CorePluginUtils.combineFilePath("directory/", "file.txt"));
    }

    /**
     * Tests retrieving Strings from the supplied action data.
     */
    @Test
    @DisplayName("Tests retrieving Strings from the data.")
    void testGetString() {
        assertNull(CorePluginUtils.getString(null, "{{test}}"));
        assertNull(CorePluginUtils.getString(testData, null));
        assertEquals("original-input", CorePluginUtils.getString(testData, "original-input"));
        assertEquals("job-id", CorePluginUtils.getString(testData, "{{" + DataKey.META.getKey() + "."
            + DataKey.JOB_ID.getKey() + "}}"));
        assertEquals("1122334455", CorePluginUtils.getString(testData, "{{" + DataKey.TIMESTAMP.getKey() + "}}"));
    }

    /**
     * Tests deep cloning a map.
     */
    @Test
    @DisplayName("Tests deep cloning JSON.")
    @SuppressWarnings("unchecked")
    void testClone() {
        Map<String, Object> map = Map.of("key", "value", "map", Map.of("number", 42));
        Map<String, Object> clone = CorePluginUtils.clone(map);
        assertEquals("value", clone.get("key"));
        assertEquals(42, ((Map<String, Object>) clone.get("map")).get("number"));
        clone.clear();
        assertEquals(2, map.size());
        assertTrue(map.get("map") instanceof Map);
    }

    /**
     * Tests converting a JSON string into a map.
     */
    @Test
    @DisplayName("Tests converting a JSON string into a map.")
    void testGetData() {
        String json = "{\"a\": \"b\", \"c\": 23}";
        Map<String, Object> data = CorePluginUtils.getData(json);
        assertEquals("b", data.get("a"));
        assertEquals(23, data.get("c"));
    }

}

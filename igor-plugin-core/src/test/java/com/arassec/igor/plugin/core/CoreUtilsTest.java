package com.arassec.igor.plugin.core;


import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.plugin.core.util.trigger.ManualTrigger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests the {@link CoreUtils}.
 */
@DisplayName("'Common Utils' tests.")
class CoreUtilsTest {

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
        assertEquals("file.txt", CoreUtils.appendSuffixIfRequired("file.txt", null, false));
        assertEquals("file", CoreUtils.appendSuffixIfRequired("file", ".igor", false));
        assertEquals("file.igor", CoreUtils.appendSuffixIfRequired("file", "igor", true));
        assertEquals("file.igor", CoreUtils.appendSuffixIfRequired("file", ".igor", true));
    }

    /**
     * Tests combining a path and a filename to an absolute path.
     */
    @Test
    @DisplayName("Tests combining a path and a filename to an absolute path.")
    void testCombineFilePath() {
        assertEquals("/file.txt", CoreUtils.combineFilePath(null, "file.txt"));
        assertEquals("directory/file.txt", CoreUtils.combineFilePath("directory", "file.txt"));
        assertEquals("directory/file.txt", CoreUtils.combineFilePath("directory/", "file.txt"));
    }

    /**
     * Tests retrieving Strings from the supplied action data.
     */
    @Test
    @DisplayName("Tests retrieving Strings from the data.")
    void testGetString() {
        assertNull(CoreUtils.getString(null, "{{test}}"));
        assertNull(CoreUtils.getString(testData, null));
        assertEquals("original-input", CoreUtils.getString(testData, "original-input"));
        assertEquals("job-id", CoreUtils.getString(testData, "{{" + DataKey.META.getKey() + "."
            + DataKey.JOB_ID.getKey() + "}}"));
        assertEquals("1122334455", CoreUtils.getString(testData, "{{" + DataKey.TIMESTAMP.getKey() + "}}"));
    }
}

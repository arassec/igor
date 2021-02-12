package com.arassec.igor.plugin.core;


import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.starter.DefaultJobStarter;
import com.arassec.igor.core.model.trigger.Trigger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CoreUtils}.
 */
@DisplayName("'Common Utils' tests.")
class CoreUtilsTest {

    /**
     * The data for testing.
     */
    private static final Map<String, Object> testData = new HashMap<>();

    /**
     * Initializes the test environment.
     */
    @BeforeAll
    static void initialize() {
        Trigger triggerMock = mock(Trigger.class);
        when(triggerMock.getMetaData()).thenReturn(Map.of(DataKey.SIMULATION.getKey(), true));

        Map<String, Object> meta = DefaultJobStarter.createMetaData("job-id", triggerMock);
        Map<String, Object> data = new HashMap<>();

        testData.put(DataKey.META.getKey(), meta);
        testData.put(DataKey.DATA.getKey(), data);
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

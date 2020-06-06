package com.arassec.igor.core.model.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.Task;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link BaseAction} model.
 */
@DisplayName("Base-Action Tests")
class BaseActionTest {

    /**
     * The class under test.
     */
    private static final BaseAction baseAction = mock(BaseAction.class, CALLS_REAL_METHODS);

    /**
     * The data for testing.
     */
    private static final Map<String, Object> testData = new HashMap<>();

    /**
     * Some arbitrary JSON data.
     */
    private static final Map<String, Object> wrongData = new HashMap<>();

    /**
     * Initializes the test environment.
     */
    @BeforeAll
    static void initialize() {
        Map<String, Object> meta = Task.createMetaData("job-id", "task-id");
        Map<String, Object> data = new HashMap<>();
        data.put(DataKey.SIMULATION.getKey(), true);

        testData.put(DataKey.META.getKey(), meta);
        testData.put(DataKey.DATA.getKey(), data);
        testData.put(DataKey.TIMESTAMP.getKey(), 1234567890);

        wrongData.put("foo", "bar");

        baseAction.setJsonPathConfiguration(BaseAction.DEFAULT_JSONPATH_CONFIG);
    }

    /**
     * Tests retrieving the job ID from the supplied data.
     */
    @Test
    @DisplayName("Tests retrieving the job's ID from the action data.")
    void testGetJobId() {
        assertEquals("job-id", baseAction.getJobId(testData));
        assertThrows(IllegalStateException.class, () -> baseAction.getJobId(null));
        Map<String, Object> map = Map.of();
        assertThrows(IllegalStateException.class, () -> baseAction.getJobId(map));
        assertThrows(IllegalStateException.class, () -> baseAction.getJobId(wrongData));
    }

    /**
     * Tests retrieving the task ID from the supplied data.
     */
    @Test
    @DisplayName("Tests retrieving the task's ID from the action data.")
    void testGetTaskId() {
        assertEquals("task-id", baseAction.getTaskId(testData));
        assertThrows(IllegalStateException.class, () -> baseAction.getTaskId(null));
        Map<String, Object> map = Map.of();
        assertThrows(IllegalStateException.class, () -> baseAction.getTaskId(map));
        assertThrows(IllegalStateException.class, () -> baseAction.getTaskId(wrongData));
    }

    /**
     * Tests if the action can retrieve simulation state information from the meta-data.
     */
    @Test
    @DisplayName("Tests if the action can determine the simulation state.")
    void testIsSimulation() {
        assertFalse(baseAction.isSimulation(null));
        assertFalse(baseAction.isSimulation(new HashMap<>()));
        assertTrue(baseAction.isSimulation(testData));
        assertFalse(baseAction.isSimulation(wrongData));
    }

    /**
     * Tests if the action is able to spot a JSON-Path query.
     */
    @Test
    @DisplayName("Tests the actin's ability to spot JSON-Path queries.")
    void testIsQuery() {
        assertFalse(baseAction.isQuery(null));
        assertFalse(baseAction.isQuery(""));
        assertFalse(baseAction.isQuery("foo"));
        assertTrue(baseAction.isQuery("$"));
    }

    /**
     * Tests retrieving Strings from the supplied action data.
     */
    @Test
    @DisplayName("Tests retrieving Strings from the data.")
    void testGetString() {
        assertNull(baseAction.getString(null, "$.test"));
        assertNull(baseAction.getString(testData, null));
        assertEquals("original-input", baseAction.getString(testData, "original-input"));
        assertEquals("job-id", baseAction.getString(testData, "$." + DataKey.META.getKey() + "." + DataKey.JOB_ID.getKey()));
        assertEquals("1234567890", baseAction.getString(testData, "$." + DataKey.TIMESTAMP.getKey()));
    }

    /**
     * Tests setting and getting the action's name.
     */
    @Test
    @DisplayName("Tests setting and getting the action's name.")
    void testSetGetName() {
        assertNull(baseAction.getName());
        baseAction.setName("base-action-test");
        assertEquals("base-action-test", baseAction.getName());
    }

    /**
     * Tests setting and getting the action's number of threads.
     */
    @Test
    @DisplayName("Tests setting and getting the action's number of threads.")
    @SneakyThrows
    void testSetGetNumThreads() {
        assertEquals(0, baseAction.getNumThreads());
        baseAction.setNumThreads(5);
        assertEquals(5, baseAction.getNumThreads());
    }

    /**
     * Tests setting and getting the action's 'active' property.
     */
    @Test
    @DisplayName("Tests setting and getting the action's 'active' property.")
    @SneakyThrows
    void testSetGetActive() {
        assertFalse(baseAction.isActive());
        baseAction.setActive(true);
        assertTrue(baseAction.isActive());
    }

}

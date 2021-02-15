package com.arassec.igor.core.model.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.trigger.EventTrigger;
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
        Map<String, Object> meta = new HashMap<>();
        meta.put(DataKey.JOB_ID.getKey(), "job-id");
        meta.put(DataKey.SIMULATION.getKey(), true);
        Map<String, Object> data = new HashMap<>();

        testData.put(DataKey.META.getKey(), meta);
        testData.put(DataKey.DATA.getKey(), data);
        testData.put(DataKey.TIMESTAMP.getKey(), 1234567890);

        wrongData.put("foo", "bar");
    }

    /**
     * Tests that always an empty list is returned by default when completing an action.
     */
    @Test
    @DisplayName("Tests that always an empty list is returned by default when completing an action.")
    void testComplete() {
        assertTrue(baseAction.complete().isEmpty());
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
     * Tests setting and getting the action's description.
     */
    @Test
    @DisplayName("Tests setting and getting the action's description.")
    void testSetGetDescription() {
        assertNull(baseAction.getDescription());
        baseAction.setDescription("base-action-test");
        assertEquals("base-action-test", baseAction.getDescription());
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

    /**
     * Tests setting and getting the action's 'processing finished' property.
     */
    @Test
    @DisplayName("Tests setting and getting the action's 'processing finished' property.")
    void testSetGetProcessingFinishedCallback() {
        assertNull(baseAction.getProcessingFinishedCallback());
        EventTrigger processingFinishedCallback = mock(EventTrigger.class);
        baseAction.setProcessingFinishedCallback(processingFinishedCallback);
        assertEquals(processingFinishedCallback, baseAction.getProcessingFinishedCallback());
    }

    /**
     * Tests the default value of event support.
     */
    @Test
    @DisplayName("Tests the default value of event support.")
    void testSupportsEvents() {
        assertTrue(baseAction.supportsEvents());
    }

}

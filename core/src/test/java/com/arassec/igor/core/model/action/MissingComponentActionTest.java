package com.arassec.igor.core.model.action;


import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.util.IgorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link MissingComponentAction}.
 */
@DisplayName("Missing-Component-Action tests")
class MissingComponentActionTest {

    /**
     * Tests creation of the component.
     */
    @Test
    @DisplayName("Tests creation of the component.")
    void testCreation() {
        MissingComponentAction component = new MissingComponentAction("unit-test-error-cause");
        assertEquals("core", component.getCategoryId());
        assertEquals("missing-component-action", component.getTypeId());
        assertEquals("unit-test-error-cause", component.getErrorCause());
        assertTrue(component.isActive());
        assertTrue(component.getUnEditableProperties().contains("numThreads"));
    }

    /**
     * Tests that processing data items always fails.
     */
    @Test
    @DisplayName("Tests that processing data items always fails.")
    void testProzessAlwaysFails() {
        MissingComponentAction component = new MissingComponentAction("unit-test-error-cause");
        assertThrows(IgorException.class, () -> component.process(null, null));

        Map<String, Object> data = Map.of();
        JobExecution jobExecution = new JobExecution();
        assertThrows(IgorException.class, () -> component.process(data, jobExecution));
    }

}

package com.arassec.igor.core.model.trigger;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link MissingComponentTrigger}.
 */
@DisplayName("Missing-Component-Trigger tests")
class MissingComponentTriggerTest {

    /**
     * Tests creation of the component.
     */
    @Test
    @DisplayName("Tests creation of the component.")
    void testCreation() {
        MissingComponentTrigger component = new MissingComponentTrigger("unit-test-error-cause");
        assertEquals("core", component.getCategoryId());
        assertEquals("missing-component-trigger", component.getTypeId());
        assertEquals("unit-test-error-cause", component.getErrorCause());
    }

}

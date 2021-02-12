package com.arassec.igor.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link IgorApplicationProperties}.
 */
@DisplayName("Igor's core-properties Tests")
class IgorCorePropertiesTest {

    /**
     * Tests the core properties default values.
     */
    @Test
    @DisplayName("Tests the core properties default values.")
    void testCorePropertiesDefaults() {
        IgorApplicationProperties igorCoreProperties = new IgorApplicationProperties();
        assertEquals(5, igorCoreProperties.getJobQueueSize());
    }

}

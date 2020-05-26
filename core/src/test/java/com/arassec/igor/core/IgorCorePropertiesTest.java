package com.arassec.igor.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link IgorCoreProperties}.
 */
@DisplayName("Igor's core-properties Tests")
class IgorCorePropertiesTest {

    /**
     * Tests the core properties default values.
     */
    @Test
    @DisplayName("Tests the core properties default values.")
    void testCorePropertiesDefaults() {
        IgorCoreProperties igorCoreProperties = new IgorCoreProperties();
        assertEquals(5, igorCoreProperties.getJobQueueSize());
    }

}

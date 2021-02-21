package com.arassec.igor.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link IgorApplicationProperties}.
 */
@DisplayName("Igor's application-properties Tests")
class IgorApplicationPropertiesTest {

    /**
     * Tests the core properties default values.
     */
    @Test
    @DisplayName("Tests the application properties default values.")
    void testApplicationPropertiesDefaults() {
        IgorApplicationProperties igorApplicationProperties = new IgorApplicationProperties();
        assertEquals(5, igorApplicationProperties.getJobQueueSize());
    }

}

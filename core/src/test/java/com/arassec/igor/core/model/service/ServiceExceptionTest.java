package com.arassec.igor.core.model.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link ServiceException}.
 */
@DisplayName("Service-Exception Tests")
public class ServiceExceptionTest {

    /**
     * Tests the service-exception's constructors.
     */
    @Test
    @DisplayName("Tests the service-exception's constructors.")
    void testServiceException() {
        ServiceException serviceException = new ServiceException("test-message");
        assertEquals("test-message", serviceException.getMessage());

        IllegalStateException illegalStateException = new IllegalStateException("test-exception");

        serviceException = new ServiceException("second-message", illegalStateException);
        assertEquals("second-message", serviceException.getMessage());
        assertEquals(illegalStateException, serviceException.getCause());
    }

}

package com.arassec.igor.core.model.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link BaseService} model.
 */
@DisplayName("Base-Service Tests")
class BaseServiceTest {

    /**
     * The class under test.
     */
    private BaseService baseService = mock(BaseService.class, CALLS_REAL_METHODS);

    /**
     * Tests the base-service's name handling.
     */
    @Test
    @DisplayName("Tests name handling.")
    void testNameHandling() {
        baseService.setName("test-service");
        assertEquals("test-service", baseService.getName());
    }

}

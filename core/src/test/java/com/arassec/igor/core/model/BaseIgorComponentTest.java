package com.arassec.igor.core.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link BaseIgorComponent} model.
 */
@DisplayName("Base-IgorComponent Tests")
class BaseIgorComponentTest {

    /**
     * The class under test.
     */
    private BaseIgorComponent baseIgorComponent = mock(BaseIgorComponent.class, CALLS_REAL_METHODS);

    /**
     * Tests ID handling.
     */
    @Test
    @DisplayName("Tests ID handling of the base igor-component.")
    void testIdHandling() {
        baseIgorComponent.setId("test-id");
        assertEquals("test-id", baseIgorComponent.getId());
    }

}

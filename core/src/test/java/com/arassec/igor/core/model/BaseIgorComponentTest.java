package com.arassec.igor.core.model;

import com.arassec.igor.core.model.job.execution.JobExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    private final BaseIgorComponent baseIgorComponent = mock(BaseIgorComponent.class, CALLS_REAL_METHODS);

    /**
     * Tests ID handling.
     */
    @Test
    @DisplayName("Tests ID handling of the base igor-component.")
    void testIdHandling() {
        baseIgorComponent.setId("test-id");
        assertEquals("test-id", baseIgorComponent.getId());
    }

    /**
     * Tests initialization.
     */
    @Test
    @DisplayName("Tests initialization.")
    void testInitialize() {
        assertDoesNotThrow(() -> baseIgorComponent.initialize(JobExecution.builder().jobId("job-id").build()));
    }

    /**
     * Tests shutdown.
     */
    @Test
    @DisplayName("Tests shutdown.")
    void testShutdown() {
        assertDoesNotThrow(() -> baseIgorComponent.shutdown(JobExecution.builder().jobId("job-id").build()));
    }

}

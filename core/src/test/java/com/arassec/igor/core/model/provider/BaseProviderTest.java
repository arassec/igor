package com.arassec.igor.core.model.provider;

import com.arassec.igor.core.model.job.execution.JobExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link BaseProvider} model.
 */
@DisplayName("Base-Provider Tests")
class BaseProviderTest {

    /**
     * The class under test.
     */
    private final BaseProvider baseProvider = mock(BaseProvider.class, CALLS_REAL_METHODS);

    /**
     * Tests provider initialization.
     */
    @Test
    @DisplayName("Tests provider initialization.")
    void testInitialize() {
        JobExecution jobExecution = new JobExecution();
        baseProvider.initialize("job-id", "task-id", jobExecution);
        assertEquals("job-id", baseProvider.getJobId());
        assertEquals("task-id", baseProvider.getTaskId());
        assertEquals(jobExecution, baseProvider.getJobExecution());
    }

}

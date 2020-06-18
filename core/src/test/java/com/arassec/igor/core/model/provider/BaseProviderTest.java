package com.arassec.igor.core.model.provider;

import com.arassec.igor.core.model.job.execution.JobExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link BaseProvider} model.
 */
@DisplayName("Base-Provider Tests")
class BaseProviderTest {

    /**
     * The class under test.
     */
    private final BaseProvider baseProvider = mock(BaseProvider.class,
            withSettings().useConstructor("category-id", "type-id").defaultAnswer(CALLS_REAL_METHODS));

    /**
     * Tests the {@link BaseProvider}'s constructor.
     */
    @Test
    @DisplayName("Tests the base-provider's constructor.")
    void testBaseProvider() {
        assertEquals("category-id", baseProvider.getCategoryId());
        assertEquals("type-id", baseProvider.getTypeId());
    }

    /**
     * Tests provider initialization.
     */
    @Test
    @DisplayName("Tests provider initialization.")
    void testInitialize() {
        JobExecution jobExecution = new JobExecution();
        baseProvider.initialize("job-id", jobExecution);
        assertEquals("job-id", baseProvider.getJobId());
        assertEquals(jobExecution, baseProvider.getJobExecution());
    }

}

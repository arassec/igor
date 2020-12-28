package com.arassec.igor.plugin.core.web.connector;

import com.arassec.igor.core.model.job.execution.JobExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the {@link FallbackHttpConnector}.
 */
@DisplayName("Fallback-Http-Connector Tests.")
class FallbackHttpConntectorTest {

    /**
     * Tests method invocation.
     */
    @Test
    @DisplayName("Tests that every method invocation throws an exception.")
    void testMethodInvocations() {
        FallbackHttpConnector connector = new FallbackHttpConnector();

        JobExecution jobExecution = new JobExecution();

        assertThrows(IllegalStateException.class, () -> connector.initialize("job-id", jobExecution));
        assertThrows(IllegalStateException.class, connector::testConfiguration);
        assertThrows(IllegalStateException.class, connector::getHttpClient);
    }

}

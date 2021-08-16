package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Tests the {@link IgorConnectorUtil}.
 */
@DisplayName("Igor-Component-Util Tests")
class IgorConnectorUtilTest {

    /**
     * IgorComponent for testing the util.
     */
    private TestAction testAction;

    /**
     * A connector for testing.
     */
    private Connector testConnector;

    /**
     * An inherited connector for testing.
     */
    private Connector inheritedTestConnector;

    /**
     * A {@link JobExecution} for testing.
     */
    private final JobExecution jobExecution = JobExecution.builder().jobId("job-id").build();

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        testAction = new TestAction();

        inheritedTestConnector = mock(Connector.class);
        testAction.setInheritedTestConnector(inheritedTestConnector);

        testConnector = mock(Connector.class);
        testAction.setTestConnector(testConnector);
    }

    /**
     * Tests connector initialization
     */
    @Test
    @DisplayName("Tests connector initialization")
    void testInitializeConnector() {
        // Initialize should handle null-input:
        IgorConnectorUtil.initializeConnectors(null, jobExecution);

        IgorConnectorUtil.initializeConnectors(testAction, jobExecution);
        verify(inheritedTestConnector, times(1)).initialize(jobExecution);
        verify(testConnector, times(1)).initialize(jobExecution);
    }

    /**
     * Tests connector shutdown
     */
    @Test
    @DisplayName("Tests connector shutdown")
    void testShutdownConnectors() {
        // Shutdown should handle null-input:
        IgorConnectorUtil.shutdownConnectors(null, jobExecution);

        IgorConnectorUtil.shutdownConnectors(testAction, jobExecution);
        verify(inheritedTestConnector, times(1)).shutdown(jobExecution);
        verify(testConnector, times(1)).shutdown(jobExecution);
    }

    /**
     * Action for testing.
     */
    private static class TestAction extends TestBaseAction {

        /**
         * Test connector.
         */
        @Getter
        @Setter
        @IgorParam
        private Connector testConnector;

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
            return List.of();
        }

    }

    /**
     * Base class to test connector processing on inherited fields.
     */
    private static abstract class TestBaseAction extends BaseAction {

        /**
         * Base test connector. Inherited connectors must be initialized and shut down, too.
         */
        @Getter
        @Setter
        @IgorParam
        private Connector inheritedTestConnector;

    }

}

package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.execution.JobExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link IgorComponentUtil}.
 */
@DisplayName("Igor-Component-Util Tests")
class IgorComponentUtilTest {

    /**
     * IgorComponent for testing the util.
     */
    private Action testAction;

    /**
     * A connector for testing.
     */
    private Connector testConnector;

    /**
     * A {@link JobExecution} for testing.
     */
    private JobExecution jobExecution;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        testConnector = mock(Connector.class);
        testAction = new TestAction();
        ReflectionTestUtils.setField(testAction, "testConnector", testConnector);
    }

    /**
     * Tests connector shutdown
     */
    @Test
    @DisplayName("Tests connector shutdown")
    void testShutdownConnectors() {
        // Initialize should handle null-input:
        IgorComponentUtil.initializeConnectors(null, "job-id", jobExecution);

        IgorComponentUtil.initializeConnectors(testAction, "job-id", jobExecution);
        verify(testConnector, times(1)).initialize(eq("job-id"), eq(jobExecution));
    }

    /**
     * Tests connector initialization
     */
    @Test
    @DisplayName("Tests connector initialization")
    void testInitializeConnector() {
        // Shutdown should handle null-input:
        IgorComponentUtil.shutdownConnectors(null, "job-id", jobExecution);

        IgorComponentUtil.shutdownConnectors(testAction, "job-id", jobExecution);
        verify(testConnector, times(1)).shutdown(eq("job-id"), eq(jobExecution));
    }

    /**
     * Action for testing.
     */
    private static class TestAction extends BaseAction {

        /**
         * Test connector.
         */
        @IgorParam
        private Connector testConnector;

        /**
         * Creates a new component instance.
         */
        TestAction() {
            super("action-category-id", "action-type-id");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
            return List.of();
        }

    }

}

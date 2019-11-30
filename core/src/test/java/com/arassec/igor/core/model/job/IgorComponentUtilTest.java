package com.arassec.igor.core.model.job;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.service.Service;
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
     * A service for testing.
     */
    private Service testService;

    /**
     * A {@link JobExecution} for testing.
     */
    private JobExecution jobExecution;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        testService = mock(Service.class);
        testAction = new TestAction();
        ReflectionTestUtils.setField(testAction, "testService", testService);
    }

    /**
     *
     */
    @Test
    @DisplayName("Tests service shutdown")
    void testShutdownServices() {
        // Initialize should handle null-input:
        IgorComponentUtil.initializeServices(null, "job-id", "task-id", jobExecution);

        IgorComponentUtil.initializeServices(testAction, "job-id", "task-id", jobExecution);
        verify(testService, times(1)).initialize(eq("job-id"), eq("task-id"), eq(jobExecution));
    }

    /**
     *
     */
    @Test
    @DisplayName("Tests service initialization")
    void testInitializeServices() {
        // Shutdown should handle null-input:
        IgorComponentUtil.shutdownServices(null, "job-id", "task-id", jobExecution);

        IgorComponentUtil.shutdownServices(testAction, "job-id", "task-id", jobExecution);
        verify(testService, times(1)).shutdown(eq("job-id"), eq("task-id"), eq(jobExecution));
    }

    /**
     * Action for testing.
     */
    private class TestAction extends BaseAction {

        /**
         * Test service.
         */
        @IgorParam
        private Service testService;

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

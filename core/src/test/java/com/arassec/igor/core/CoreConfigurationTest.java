package com.arassec.igor.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link CoreConfiguration} functionality.
 */
public class CoreConfigurationTest {

    /**
     * Tests the configuration of the job scheduler.
     */
    @Test
    @DisplayName("Tests the configured job scheduler.")
    void testJobScheduler() {
        CoreConfiguration coreConfiguration = new CoreConfiguration();
        TaskScheduler taskScheduler = coreConfiguration.jobScheduler();
        assertTrue(taskScheduler instanceof ThreadPoolTaskScheduler);
        assertEquals("jobScheduler", ((ThreadPoolTaskScheduler) taskScheduler).getThreadNamePrefix());
        assertEquals(10, ((ThreadPoolTaskScheduler) taskScheduler).getPoolSize());
    }

    /**
     * Tests for required annotations.
     */
    @Test
    @DisplayName("Tests required annotations are present.")
    void testAnnotations() {
        assertTrue(CoreConfiguration.class.isAnnotationPresent(Configuration.class));
        assertTrue(CoreConfiguration.class.isAnnotationPresent(EnableScheduling.class));
    }

}

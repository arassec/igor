package com.arassec.igor.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link ApplicationConfiguration} functionality.
 */
class ApplicationConfigurationTest {

    /**
     * Tests the configuration of the job scheduler.
     */
    @Test
    @DisplayName("Tests the configured job scheduler.")
    void testJobScheduler() {
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
        TaskScheduler taskScheduler = applicationConfiguration.jobScheduler();
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
        assertTrue(ApplicationConfiguration.class.isAnnotationPresent(Configuration.class));
        assertTrue(ApplicationConfiguration.class.isAnnotationPresent(EnableScheduling.class));
    }

    /**
     * Tests I18N of core messages.
     */
    @Test
    @DisplayName("Tests I18N of core messages.")
    void testI18n() {
        ApplicationConfiguration configuration = new ApplicationConfiguration();
        MessageSource messageSource = configuration.coreMessageSource();
        assertEquals("a job with the same name already exists",
                messageSource.getMessage("com.arassec.igor.core.validation.unique-job-name", null, Locale.getDefault()));
    }

}

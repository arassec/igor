package com.arassec.igor.core;

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

    /**
     * Tests I18N.
     */
    @Test
    @DisplayName("Tests module-misc I18N.")
    void testI18n() {
        CoreConfiguration configuration = new CoreConfiguration();
        MessageSource messageSource = configuration.coreMessageSource();
        assertEquals("a job with the same name already exists",
                messageSource.getMessage("com.arassec.igor.validation.unique-job-name", null, Locale.getDefault()));
    }

}

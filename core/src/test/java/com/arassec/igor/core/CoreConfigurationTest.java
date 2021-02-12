package com.arassec.igor.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link CoreConfiguration} functionality.
 */
class CoreConfigurationTest {

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
    @DisplayName("Tests module-core I18N.")
    void testI18n() {
        CoreConfiguration configuration = new CoreConfiguration();
        MessageSource messageSource = configuration.coreMessageSource();
        assertEquals("a job with the same name already exists",
                messageSource.getMessage("com.arassec.igor.core.validation.unique-job-name", null, Locale.getDefault()));
    }

}

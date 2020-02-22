package com.arassec.igor.module.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link ModuleFileConfiguration}.
 */
@DisplayName("'Module File'-configuration tests.")
public class ModuleFileConfigurationTest {

    /**
     * Tests I18N of the misc-module.
     */
    @Test
    @DisplayName("Tests module-file I18N.")
    void testI18n() {
        ModuleFileConfiguration configuration = new ModuleFileConfiguration();
        MessageSource messageSource = configuration.fileMessageSource();
        assertEquals("Copy file", messageSource.getMessage("d8564415-7dd9-4814-9b46-c2c5b56ed5cc", null, Locale.getDefault()));
    }

}
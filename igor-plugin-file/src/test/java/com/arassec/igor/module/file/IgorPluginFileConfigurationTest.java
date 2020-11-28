package com.arassec.igor.module.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link IgorPluginFileConfiguration}.
 */
@DisplayName("'File Plugin'-configuration tests.")
class IgorPluginFileConfigurationTest {

    /**
     * Tests I18N of the file plugin.
     */
    @Test
    @DisplayName("Tests file plugin I18N.")
    void testI18n() {
        IgorPluginFileConfiguration configuration = new IgorPluginFileConfiguration();
        MessageSource messageSource = configuration.pluginFileMessageSource();
        assertEquals("SFTP", messageSource.getMessage("sftp-file-connector", null, Locale.getDefault()));
    }

}

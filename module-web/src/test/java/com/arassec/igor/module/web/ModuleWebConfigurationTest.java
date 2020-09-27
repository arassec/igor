package com.arassec.igor.module.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link ModuleWebConfiguration}.
 */
@DisplayName("'Module Web'-configuration tests.")
class ModuleWebConfigurationTest {

    /**
     * Tests I18N of the web-module.
     */
    @Test
    @DisplayName("Tests module-web I18N.")
    void testI18n() {
        ModuleWebConfiguration configuration = new ModuleWebConfiguration();
        MessageSource messageSource = configuration.webMessageSource();
        assertEquals("HTTP", messageSource.getMessage("http-web-connector", null, Locale.getDefault()));
    }

}

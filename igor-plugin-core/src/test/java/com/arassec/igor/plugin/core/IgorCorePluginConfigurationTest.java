package com.arassec.igor.plugin.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link IgorCorePluginConfiguration}.
 */
@DisplayName("'Core Plugin'-configuration tests.")
class IgorCorePluginConfigurationTest {

    /**
     * Tests I18N of the common plugin.
     */
    @Test
    @DisplayName("Tests core plugin I18N.")
    void testI18n() {
        IgorCorePluginConfiguration configuration = new IgorCorePluginConfiguration();
        MessageSource messageSource = configuration.pluginCoreMessageSource();
        assertEquals("Filter by Regular Expression", messageSource.getMessage("filter-by-regexp-action", null, Locale.getDefault()));
    }

}

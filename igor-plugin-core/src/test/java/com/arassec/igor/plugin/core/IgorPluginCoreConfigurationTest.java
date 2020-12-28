package com.arassec.igor.plugin.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link IgorPluginCoreConfiguration}.
 */
@DisplayName("'Core Plugin'-configuration tests.")
class IgorPluginCoreConfigurationTest {

    /**
     * Tests I18N of the common plugin.
     */
    @Test
    @DisplayName("Tests core plugin I18N.")
    void testI18n() {
        IgorPluginCoreConfiguration configuration = new IgorPluginCoreConfiguration();
        MessageSource messageSource = configuration.pluginCoreMessageSource();
        assertEquals("Filter by Regular Expression", messageSource.getMessage("filter-by-regexp-action", null, Locale.getDefault()));
    }

}

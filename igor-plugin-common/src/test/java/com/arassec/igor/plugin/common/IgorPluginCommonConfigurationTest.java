package com.arassec.igor.plugin.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link IgorPluginCommonConfiguration}.
 */
@DisplayName("'Common Plugin'-configuration tests.")
class IgorPluginCommonConfigurationTest {

    /**
     * Tests I18N of the common plugin.
     */
    @Test
    @DisplayName("Tests common plugin I18N.")
    void testI18n() {
        IgorPluginCommonConfiguration configuration = new IgorPluginCommonConfiguration();
        MessageSource messageSource = configuration.pluginCommonMessageSource();
        assertEquals("Filter by Regular Expression", messageSource.getMessage("filter-by-regexp-action", null, Locale.getDefault()));
    }

}

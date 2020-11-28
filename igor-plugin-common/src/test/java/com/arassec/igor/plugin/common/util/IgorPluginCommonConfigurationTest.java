package com.arassec.igor.plugin.common.util;

import com.arassec.igor.plugin.common.IgorPluginCommonConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link IgorPluginCommonConfiguration}.
 */
@DisplayName("'Module Misc'-configuration tests.")
class IgorPluginCommonConfigurationTest {

    /**
     * Tests I18N of the misc-module.
     */
    @Test
    @DisplayName("Tests module-misc I18N.")
    void testI18n() {
        IgorPluginCommonConfiguration configuration = new IgorPluginCommonConfiguration();
        MessageSource messageSource = configuration.pluginCommonMessageSource();
        assertEquals("Sort by Timestamp Pattern", messageSource.getMessage("sort-by-timestamp-pattern-action", null,
                Locale.getDefault()));
    }

}
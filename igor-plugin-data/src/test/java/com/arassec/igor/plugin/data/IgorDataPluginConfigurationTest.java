package com.arassec.igor.plugin.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link IgorDataPluginConfiguration}.
 */
@DisplayName("'Data Plugin'-configuration tests.")
class IgorDataPluginConfigurationTest {

    /**
     * Tests I18N of the data plugin.
     */
    @Test
    @DisplayName("Tests data plugin I18N.")
    void testI18n() {
        IgorDataPluginConfiguration configuration = new IgorDataPluginConfiguration();
        MessageSource messageSource = configuration.dataMessageSource();
        assertEquals("Commit Transaction", messageSource.getMessage("commit-transaction-action", null, Locale.getDefault()));
    }

}

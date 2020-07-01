package com.arassec.igor.module.misc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link ModuleMiscConfiguration}.
 */
@DisplayName("'Module Misc'-configuration tests.")
class ModuleMiscConfigurationTest {

    /**
     * Tests I18N of the misc-module.
     */
    @Test
    @DisplayName("Tests module-misc I18N.")
    void testI18n() {
        ModuleMiscConfiguration configuration = new ModuleMiscConfiguration();
        MessageSource messageSource = configuration.miscMessageSource();
        assertEquals("Sort by Timestamp Pattern", messageSource.getMessage("sort-by-timestamp-pattern-action", null,
                Locale.getDefault()));
    }

}
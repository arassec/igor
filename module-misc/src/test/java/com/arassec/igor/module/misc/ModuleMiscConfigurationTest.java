package com.arassec.igor.module.misc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals("Sort by timestamp pattern", messageSource.getMessage("e43efa64-d1a3-422f-ac6c-f34cd56be0c2", null, Locale.getDefault()));
    }

}
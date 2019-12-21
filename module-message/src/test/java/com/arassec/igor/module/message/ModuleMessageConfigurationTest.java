package com.arassec.igor.module.message;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the module configuration.
 */
@DisplayName("Tests the message module's configuration.")
public class ModuleMessageConfigurationTest {

    /**
     * Tests resolution of I18N keys to display texts.
     */
    @Test
    @DisplayName("Tests I18N of the message module.")
    void testI18n() {
        ModuleMessageConfiguration configuration = new ModuleMessageConfiguration();
        MessageSource messageSource = configuration.messageMessageSource();
        assertEquals("Send message", messageSource.getMessage("88a0e988-d3ec-4b91-b98c-92d99c09ba33", null, Locale.getDefault()));
    }

}

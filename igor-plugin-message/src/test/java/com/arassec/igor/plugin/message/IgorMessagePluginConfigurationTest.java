package com.arassec.igor.plugin.message;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the module configuration.
 */
@DisplayName("Tests the message plugin's configuration.")
class IgorMessagePluginConfigurationTest {

    /**
     * Tests resolution of I18N keys to display texts.
     */
    @Test
    @DisplayName("Tests I18N of the message plugin.")
    void testI18n() {
        IgorMessagePluginConfiguration configuration = new IgorMessagePluginConfiguration();
        MessageSource messageSource = configuration.messageMessageSource();
        assertEquals("RabbitMQ", messageSource.getMessage("rabbitmq-message-connector", null, Locale.getDefault()));
    }

}

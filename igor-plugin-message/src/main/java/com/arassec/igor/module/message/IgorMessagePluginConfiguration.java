package com.arassec.igor.module.message;

import com.arassec.igor.application.util.IgorConfigHelper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the igor 'message' plugin.
 */
@Configuration
@ComponentScan
public class IgorMessagePluginConfiguration {

    /**
     * Creates a new {@link MessageSource} for i18n.
     *
     * @return The newly created instance.
     */
    @Bean
    public MessageSource messageMessageSource() {
        return IgorConfigHelper.createMessageSource("i18n/plugin-message-labels");
    }

}

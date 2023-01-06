package com.arassec.igor.plugin.data;

import com.arassec.igor.application.util.IgorConfigHelper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the igor 'data' plugin.
 */
@Configuration
@ComponentScan
public class IgorDataPluginConfiguration {

    /**
     * Creates a new {@link MessageSource} for i18n.
     *
     * @return The newly created instance.
     */
    @Bean
    public MessageSource dataMessageSource() {
        return IgorConfigHelper.createMessageSource("i18n/plugin-data-labels");
    }

}

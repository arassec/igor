package com.arassec.igor.core;

import com.arassec.igor.core.util.IgorConfigHelper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Igor's core configuration.
 */
@Configuration
@ComponentScan
@EnableScheduling
@EnableConfigurationProperties(IgorApplicationProperties.class)
public class CoreConfiguration {

    /**
     * Creates a new {@link MessageSource} for i18n.
     *
     * @return The newly created instance.
     */
    @Bean
    public MessageSource coreMessageSource() {
        return IgorConfigHelper.createMessageSource("i18n/core-messages");
    }

}

package com.arassec.igor.plugin.core;

import com.arassec.igor.core.util.IgorConfigHelper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for igor's 'core' plugin.
 */
@Configuration
@ComponentScan
public class IgorPluginCoreConfiguration {

    /**
     * Creates a new {@link MessageSource} for i18n.
     *
     * @return The newly created instance.
     */
    @Bean
    public MessageSource pluginCoreMessageSource() {
        return IgorConfigHelper.createMessageSource("i18n/plugin-core-labels");
    }

}

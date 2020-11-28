package com.arassec.igor.plugin.common;

import com.arassec.igor.core.util.IgorConfigHelper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the igor's 'common' plugin.
 */
@Configuration
@ComponentScan
public class IgorPluginCommonConfiguration {

    /**
     * Creates a new {@link MessageSource} for i18n.
     *
     * @return The newly created instance.
     */
    @Bean
    public MessageSource pluginCommonMessageSource() {
        return IgorConfigHelper.createMessageSource("i18n/plugin-common-labels");
    }

}

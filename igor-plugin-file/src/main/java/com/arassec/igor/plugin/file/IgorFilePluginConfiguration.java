package com.arassec.igor.plugin.file;

import com.arassec.igor.application.util.IgorConfigHelper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the igor 'file' plugin.
 */
@Configuration
@ComponentScan
public class IgorFilePluginConfiguration {

    /**
     * Creates a new {@link MessageSource} for i18n.
     *
     * @return The newly created instance.
     */
    @Bean
    public MessageSource pluginFileMessageSource() {
        return IgorConfigHelper.createMessageSource("i18n/plugin-file-labels");
    }

}

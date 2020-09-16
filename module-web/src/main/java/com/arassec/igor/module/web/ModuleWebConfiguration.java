package com.arassec.igor.module.web;

import com.arassec.igor.core.util.IgorConfigHelper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the igor 'web' module.
 */
@Configuration
@ComponentScan
public class ModuleWebConfiguration {

    /**
     * Creates a new {@link MessageSource} for i18n.
     *
     * @return The newly created instance.
     */
    @Bean
    public MessageSource webMessageSource() {
        return IgorConfigHelper.createMessageSource("i18n/module-web-labels");
    }

}

package com.arassec.igor.module.misc;

import com.arassec.igor.core.util.IgorConfigHelper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the igor misc-module.
 */
@Configuration
public class ModuleMiscConfiguration {

    /**
     * Creates a new {@link MessageSource} for i18n.
     *
     * @return The newly created instance.
     */
    @Bean
    public MessageSource miscMessageSource() {
        return IgorConfigHelper.createMessageSource("i18n/module-misc-labels");
    }

}

package com.arassec.igor.module.file;

import com.arassec.igor.core.util.IgorConfigHelper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the igor file-module.
 */
@Configuration
public class ModuleFileConfiguration {

    /**
     * Creates a new {@link MessageSource} for i18n.
     *
     * @return The newly created instance.
     */
    @Bean
    public MessageSource fileMessageSource() {
        return IgorConfigHelper.createMessageSource("i18n/module-file-labels");
    }

}

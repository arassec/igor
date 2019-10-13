package com.arassec.igor.core.util;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Base class for configurations.
 */
public final class IgorConfigHelper {

    /**
     * Creates a {@link MessageSource} to suppoer i18n.
     *
     * @param baseNames The basenames of the properties files to use.
     *
     * @return
     */
    public static MessageSource createMessageSource(String... baseNames) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames(baseNames);
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

}

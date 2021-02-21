package com.arassec.igor.application.util;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Base class for configurations.
 */
public final class IgorConfigHelper {

    /**
     * Prevent instantiation.
     */
    private IgorConfigHelper() {
    }

    /**
     * Creates a {@link MessageSource} to suppoer i18n.
     *
     * @param baseNames The basenames of the properties files to use.
     *
     * @return A newly created {@link MessageSource}.
     */
    public static MessageSource createMessageSource(String... baseNames) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames(baseNames);
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

}

package com.arassec.igor.web.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Utility to check for and read documentation from different modules.
 */
@Slf4j
@Component
public class DocumentationUtil {

    /**
     * The base directory that contains documentation of a module.
     */
    private static final String BASE_DIR = "doc";

    /**
     * The file suffix for documentation.
     */
    private static final String DOC_SUFFIX = ".md";

    /**
     * Private constructor to prevent instantiation.
     */
    private DocumentationUtil() {
        // prevent instantiation
    }

    /**
     * Checks whether documentation for the key is available or not.
     *
     * @param key    The documentation key.
     * @param locale The locale to check for.
     *
     * @return {@code true} if documentation is available, either directly for the requested locale, or as fallback in the default
     * locale.
     */
    public static boolean isDocumentationAvailable(String key, Locale locale) {
        return getClassPathResource(key, locale).exists();
    }

    /**
     * Reads the documentation for the given key and the requested locale. If no documentation for the locale is available, the
     * fallback locale is used.
     *
     * @param key    The documentation key.
     * @param locale The locale to read the documentation in.
     *
     * @return The documentation as string or {@code null}, if none exists.
     */
    public static String readDocumentation(String key, Locale locale) {
        ClassPathResource classPathResource = getClassPathResource(key, locale);
        if (classPathResource.exists()) {
            try (InputStream inputStream = classPathResource.getInputStream()) {
                return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.warn("Documentation could not be read although it should be available!", e);
            }
        }
        return null;
    }

    /**
     * Returns a {@link ClassPathResource} to the documentation, whether it exists or not.
     *
     * @param key    The documentation key.
     * @param locale The locale of the documentation.
     *
     * @return A {@link ClassPathResource} to the documentation.
     */
    private static ClassPathResource getClassPathResource(String key, Locale locale) {
        ClassPathResource classPathResource = null;
        if (locale != null) {
            classPathResource = new ClassPathResource(BASE_DIR + File.separator + locale.getLanguage() + File.separator + key + DOC_SUFFIX);
        }
        if (classPathResource == null || !classPathResource.exists()) {
            classPathResource = new ClassPathResource(BASE_DIR + File.separator + key + DOC_SUFFIX);
        }
        return classPathResource;
    }

}

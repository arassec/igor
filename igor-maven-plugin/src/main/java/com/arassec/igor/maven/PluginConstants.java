package com.arassec.igor.maven;

/**
 * Contains constants used throughout the plugin.
 */
public final class PluginConstants {

    /**
     * Path to the module's Java sources.
     */
    public static final String JAVA_SOURCES = "/src/main/java";

    /**
     * Path to the target directory for generated documentation.
     */
    public static final String DOC_GEN_TARGET_DIR = "/src/main/resources/doc-gen/";

    /**
     * Path to the target directory for manually created documentation.
     */
    public static final String DOC_TARGET_DIR = "/src/main/resources/doc/";

    /**
     * Path to the module's I18N sources.
     */
    public static final String I18N_SOURCES = "/src/main/resources/i18n/";

    /**
     * Creates a new instance.
     */
    private PluginConstants() {
        // prevent instantiation...
    }
}

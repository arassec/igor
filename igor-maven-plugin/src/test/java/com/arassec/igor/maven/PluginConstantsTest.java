package com.arassec.igor.maven;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the plugin constants.
 */
class PluginConstantsTest {

    /**
     * Tests the values of the constants.
     */
    @Test
    void testConstantsValues() {
        assertEquals("/src/main/java", PluginConstants.JAVA_SOURCES);
        assertEquals("/src/main/resources/doc-gen/", PluginConstants.DOC_GEN_TARGET_DIR);
        assertEquals("/src/main/resources/i18n/", PluginConstants.I18N_SOURCES);
    }

}

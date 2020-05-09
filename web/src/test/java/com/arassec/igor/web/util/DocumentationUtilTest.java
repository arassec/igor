package com.arassec.igor.web.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link DocumentationUtil}.
 */
@DisplayName("Tests the documentation utility")
class DocumentationUtilTest {

    /**
     * Tests checking for available documentation.
     */
    @Test
    @DisplayName("Tests checking for available documentation.")
    void testIsDocumentationAvailable() {
        assertFalse(DocumentationUtil.isDocumentationAvailable(null, null));
        assertFalse(DocumentationUtil.isDocumentationAvailable("invalid", null));
        assertFalse(DocumentationUtil.isDocumentationAvailable(null, Locale.ENGLISH));

        assertTrue(DocumentationUtil.isDocumentationAvailable("documentation", null));
        assertTrue(DocumentationUtil.isDocumentationAvailable("documentation", Locale.ENGLISH));
        assertTrue(DocumentationUtil.isDocumentationAvailable("documentation", Locale.GERMAN));

        assertTrue(DocumentationUtil.isDocumentationAvailable("english-only", Locale.ENGLISH));
        assertTrue(DocumentationUtil.isDocumentationAvailable("english-only", Locale.GERMAN));
    }

    /**
     * Tests reading documentation.
     */
    @Test
    @DisplayName("Tests reading documentation.")
    void testReadDocumentation() {
        assertNull(DocumentationUtil.readDocumentation(null, null));
        assertNull(DocumentationUtil.readDocumentation("invalid", null));
        assertNull(DocumentationUtil.readDocumentation(null, Locale.ENGLISH));

        assertEquals("# Documentation", DocumentationUtil.readDocumentation("documentation", null));
        assertEquals("# Documentation", DocumentationUtil.readDocumentation("documentation", Locale.ENGLISH));
        assertEquals("# Dokumentation", DocumentationUtil.readDocumentation("documentation", Locale.GERMAN));

        assertEquals("# English Only", DocumentationUtil.readDocumentation("english-only", Locale.ENGLISH));
        assertEquals("# English Only", DocumentationUtil.readDocumentation("english-only", Locale.GERMAN));
    }

}
package com.arassec.igor.web.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link TypeData}.
 */
@DisplayName("Tests the type-data model.")
class TypeDataTest {

    /**
     * Tests the model class.
     */
    @Test
    @DisplayName("Tests the type-data model class.")
    void testKeyLabelStore() {
        TypeData typeData = new TypeData("key", "label", true, true);
        assertEquals("key", typeData.getKey());
        assertEquals("label", typeData.getValue());
        assertTrue(typeData.isDocumentationAvailable());
        assertTrue(typeData.isSupportsEvents());
    }

}
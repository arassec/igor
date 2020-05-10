package com.arassec.igor.web.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link KeyLabelStore}.
 */
@DisplayName("Tests the key-label-store.")
class KeyLabelStoreTest {

    /**
     * Tests the model class.
     */
    @Test
    @DisplayName("Tests the model class.")
    void testKeyLabelStore() {
        KeyLabelStore keyLabelStore = new KeyLabelStore("key", "label");
        assertEquals("key", keyLabelStore.getKey());
        assertEquals("label", keyLabelStore.getValue());
    }

}
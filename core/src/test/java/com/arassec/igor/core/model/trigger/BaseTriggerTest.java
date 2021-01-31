package com.arassec.igor.core.model.trigger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link BaseTrigger}.
 */
@DisplayName("Base-Trigger Tests.")
class BaseTriggerTest {

    /**
     * The base class under test.
     */
    private final BaseTrigger baseTrigger = mock(BaseTrigger.class,
            withSettings().useConstructor("category-id", "type-id").defaultAnswer(CALLS_REAL_METHODS));

    /**
     * Tests the {@link BaseTrigger}'s constructor.
     */
    @Test
    @DisplayName("Tests the base-trigger's constructor.")
    void testBaseTrigger() {
        assertEquals("category-id", baseTrigger.getCategoryId());
        assertEquals("type-id", baseTrigger.getTypeId());
    }

    /**
     * Tests getting meta-data.
     */
    @Test
    @DisplayName("Tests getting meta-data.")
    void testGetMetaData() {
        assertEquals(Map.of(), baseTrigger.getMetaData());
    }

    /**
     * Tests getting data.
     */
    @Test
    @DisplayName("Tests getting data.")
    void testGetData() {
        assertEquals(Map.of(), baseTrigger.getData());
    }

}

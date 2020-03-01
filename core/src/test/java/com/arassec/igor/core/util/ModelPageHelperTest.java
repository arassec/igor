package com.arassec.igor.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link ModelPageHelper} utility.
 */
@DisplayName("Modelpage-Helper Tests")
class ModelPageHelperTest {

    /**
     * Test data.
     */
    private final List<Integer> all = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    /**
     * Tests getting an empty model page.
     */
    @Test
    @DisplayName("Tests getting an empty model page.")
    void testGetEmptyModelPage() {
        ModelPage<Integer> page = ModelPageHelper.getModelPage(null, 666, 1);
        assertEquals(666, page.getNumber());
        assertEquals(0, page.getSize());
        assertEquals(0, page.getTotalPages());
        assertNotNull(page.getItems());

        page = ModelPageHelper.getModelPage(new LinkedList<>(), 666, 1);
        assertEquals(666, page.getNumber());
        assertEquals(0, page.getSize());
        assertEquals(0, page.getTotalPages());
        assertNotNull(page.getItems());
    }

    /**
     * Tests getting a complete page.
     */
    @Test
    @DisplayName("Tests getting the complete page of all items.")
    void testGetCompleteModelPage() {
        ModelPage<Integer> page = ModelPageHelper.getModelPage(all, 0, 20);
        assertEquals(0, page.getNumber());
        assertEquals(20, page.getSize());
        assertEquals(1, page.getTotalPages());
        assertEquals(10, page.getItems().size());
    }

    /**
     * Tests getting a partial page.
     */
    @Test
    @DisplayName("Tests getting a page.")
    void testGetModelPage() {
        ModelPage<Integer> page = ModelPageHelper.getModelPage(all, 1, 3);
        assertEquals(1, page.getNumber());
        assertEquals(3, page.getSize());
        assertEquals(4, page.getTotalPages());
        assertEquals(3, page.getItems().size());
        assertEquals(4, page.getItems().get(0));
        assertEquals(5, page.getItems().get(1));
        assertEquals(6, page.getItems().get(2));
    }

    /**
     * Tests input validation.
     */
    @Test
    @DisplayName("Tests input validation.")
    void testInputValidation() {
        ModelPage<Integer> page = ModelPageHelper.getModelPage(all, 1, 0);
        assertEquals(1, page.getNumber());
        assertEquals(0, page.getSize());
        assertEquals(0, page.getTotalPages());
        assertEquals(0, page.getItems().size());
    }

}

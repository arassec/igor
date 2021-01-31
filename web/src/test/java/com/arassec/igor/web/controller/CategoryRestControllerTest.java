package com.arassec.igor.web.controller;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.web.model.KeyLabelStore;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the {@link CategoryRestController}.
 */
@DisplayName("Category-Controller Tests")
class CategoryRestControllerTest extends RestControllerBaseTest {

    /**
     * Tests retrieval of connector categories.
     */
    @Test
    @DisplayName("Tests retrieval of connector categories.")
    @SneakyThrows(Exception.class)
    void testGetConnectorCategories() {
        when(igorComponentRegistry.getCategoriesOfComponentType(Connector.class)).thenReturn(Set.of("two", "one"));

        MvcResult mvcResult = mockMvc.perform(get("/api/category/connector")).andExpect(status().isOk()).andReturn();

        List<KeyLabelStore> result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(2, result.size());
        assertEquals(new KeyLabelStore("one", "alpha"), result.get(0));
        assertEquals(new KeyLabelStore("two", "beta"), result.get(1));
    }

    /**
     * Tests retrieval of action categories.
     */
    @Test
    @DisplayName("Tests retrieval of action categories.")
    @SneakyThrows(Exception.class)
    void testGetActionCategories() {
        when(igorComponentRegistry.getCategoriesOfComponentType(Action.class)).thenReturn(Set.of("two", "one"));

        MvcResult mvcResult = mockMvc.perform(get("/api/category/action")).andExpect(status().isOk()).andReturn();

        List<KeyLabelStore> result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(2, result.size());
        assertEquals(new KeyLabelStore("one", "alpha"), result.get(0));
        assertEquals(new KeyLabelStore("two", "beta"), result.get(1));
    }

    /**
     * Tests retrieval of trigger categories.
     */
    @Test
    @DisplayName("Tests retrieval of trigger categories.")
    @SneakyThrows(Exception.class)
    void testGetTriggerCategories() {
        when(igorComponentRegistry.getCategoriesOfComponentType(Trigger.class)).thenReturn(Set.of("two", "one"));

        MvcResult mvcResult = mockMvc.perform(get("/api/category/trigger")).andExpect(status().isOk()).andReturn();

        List<KeyLabelStore> result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(2, result.size());
        assertEquals(new KeyLabelStore("one", "alpha"), result.get(0));
        assertEquals(new KeyLabelStore("two", "beta"), result.get(1));
    }

}
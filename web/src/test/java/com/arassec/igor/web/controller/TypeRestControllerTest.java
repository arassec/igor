package com.arassec.igor.web.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the {@link TypeRestController}.
 */
@DisplayName("Type-Controller Tests")
class TypeRestControllerTest extends RestControllerBaseTest {

    /**
     * Tests retrieval of action types.
     */
    @Test
    @DisplayName("Tests retrieval of action types.")
    @SneakyThrows(Exception.class)
    void testGetActionTypes() {
        when(igorComponentRegistry.getActionTypesOfCategory(eq("123abc456"))).thenReturn(Set.of("two", "one"));

        mockMvc.perform(get("/api/type/action/123abc456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("one"))
                .andExpect(jsonPath("$[0].value").value("alpha"))
                .andExpect(jsonPath("$[0].documentationAvailable").value(false))
                .andExpect(jsonPath("$[1].key").value("two"))
                .andExpect(jsonPath("$[1].value").value("beta"))
                .andExpect(jsonPath("$[1].documentationAvailable").value(false));
    }

    /**
     * Tests retrieval of trigger types.
     */
    @Test
    @DisplayName("Tests retrieval of trigger types.")
    @SneakyThrows(Exception.class)
    void testGetTriggerTypes() {
        when(igorComponentRegistry.getTriggerTypesOfCategory(eq("123abc456"))).thenReturn(Set.of("two", "one"));

        mockMvc.perform(get("/api/type/trigger/123abc456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("one"))
                .andExpect(jsonPath("$[0].value").value("alpha"))
                .andExpect(jsonPath("$[0].documentationAvailable").value(false))
                .andExpect(jsonPath("$[1].key").value("two"))
                .andExpect(jsonPath("$[1].value").value("beta"))
                .andExpect(jsonPath("$[1].documentationAvailable").value(false));
    }

    /**
     * Tests retrieval of connector types.
     */
    @Test
    @DisplayName("Tests retrieval of connector types.")
    @SneakyThrows(Exception.class)
    void testGetConnectorTypes() {
        when(igorComponentRegistry.getConnectorTypesOfCategory(eq("123abc456"))).thenReturn(Set.of("two", "one"));

        mockMvc.perform(get("/api/type/connector/123abc456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("one"))
                .andExpect(jsonPath("$[0].value").value("alpha"))
                .andExpect(jsonPath("$[0].documentationAvailable").value(false))
                .andExpect(jsonPath("$[1].key").value("two"))
                .andExpect(jsonPath("$[1].value").value("beta"))
                .andExpect(jsonPath("$[1].documentationAvailable").value(false));
    }

}
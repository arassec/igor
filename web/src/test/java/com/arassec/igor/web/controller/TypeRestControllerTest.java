package com.arassec.igor.web.controller;

import com.arassec.igor.web.model.KeyLabelStore;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
     * Tests retrieval of types.
     */
    @Test
    @DisplayName("Tests retrieval of types.")
    @SneakyThrows(Exception.class)
    void testGetTypes() {
        when(igorComponentRegistry.getTypesOfCategory(eq("123abc456"))).thenReturn(Set.of("two", "one"));

        mockMvc.perform(get("/api/type/123abc456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("one"))
                .andExpect(jsonPath("$[0].value").value("alpha"))
                .andExpect(jsonPath("$[0].documentationAvailable").value(false))
                .andExpect(jsonPath("$[1].key").value("two"))
                .andExpect(jsonPath("$[1].value").value("beta"))
                .andExpect(jsonPath("$[1].documentationAvailable").value(false));
    }

}
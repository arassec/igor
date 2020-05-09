package com.arassec.igor.web.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the {@link DocumentationRestController}.
 */
@DisplayName("Documentation-Controller Tests")
class DocumentationRestControllerTest extends RestControllerBaseTest {

    /**
     * Tests retrieval of documentation.
     */
    @Test
    @DisplayName("Tests retrieval of documentation.")
    @SneakyThrows(Exception.class)
    void testReadDoc() {
        mockMvc.perform(get("/api/doc/documentation"))
                .andExpect(status().isOk())
                .andExpect(content().string("# Documentation"));
    }

    /**
     * Tests retrieval of not existing documentation.
     */
    @Test
    @DisplayName("Tests retrieval of not existing documentation.")
    @SneakyThrows(Exception.class)
    void testReadNonExistingDoc() {
        mockMvc.perform(get("/api/doc/invalid")).andExpect(status().isNotFound());
    }

}
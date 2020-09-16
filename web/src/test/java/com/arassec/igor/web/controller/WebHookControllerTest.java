package com.arassec.igor.web.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the {@link WebHookController}.
 */
@DisplayName("Web-Hook-Controller Tests")
class WebHookControllerTest extends RestControllerBaseTest {

    /**
     * Tests calling a webhook with HTTP-GET.
     */
    @Test
    @DisplayName("Tests calling a webhook with HTTP-GET.")
    @SneakyThrows
    void testGet() {
        mockMvc.perform(get("/webhook/job-id")
                .param("a", "b"))
                .andExpect(status().isOk());
    }

    /**
     * Tests calling a webhook with HTTP-POST.
     */
    @Test
    @DisplayName("Tests calling a webhook with HTTP-POST.")
    @SneakyThrows
    void testPost() {
        mockMvc.perform(post("/webhook/job-id")
                .param("c", "d"))
                .andExpect(status().isOk());
    }

}

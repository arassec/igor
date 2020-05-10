package com.arassec.igor.web.controller;

import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.web.model.ConnectorListEntry;
import com.arassec.igor.web.test.TestConnector;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Base64;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the {@link ConnectorRestController}.
 */
@DisplayName("Connector-Controller Tests")
class ConnectorRestControllerTest extends RestControllerBaseTest {

    /**
     * Tests getting connectors.
     */
    @Test
    @DisplayName("Tests getting connectors.")
    @SneakyThrows(Exception.class)
    void testGetConnectors() {
        MvcResult mvcResult = mockMvc.perform(get("/api/connector")).andExpect(status().isOk()).andReturn();

        ModelPage<ConnectorListEntry> result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(0, result.getNumber());
        assertEquals(Integer.MAX_VALUE, result.getSize());
        assertEquals(0, result.getTotalPages());
        assertNotNull(result.getItems());

        TestConnector testConnector = new TestConnector();
        testConnector.setId("connector-id");
        testConnector.setName("connector-name");
        when(connectorManager.loadPage(eq(666), eq(2), eq("name-filter"))).thenReturn(
                new ModelPage<>(666, 2, 1, List.of(testConnector)));

        mvcResult = mockMvc.perform(get("/api/connector")
                .queryParam("pageNumber", "666")
                .queryParam("pageSize", "2")
                .queryParam("nameFilter", "name-filter"))
                .andExpect(status().isOk()).andReturn();

        result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(666, result.getNumber());
        assertEquals(2, result.getSize());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getItems().size());

        ConnectorListEntry connectorListEntry = result.getItems().get(0);

        assertEquals("connector-id", connectorListEntry.getId());
        assertEquals("connector-name", connectorListEntry.getName());
    }

    /**
     * Tests getting all connectors of a certain category.
     */
    @Test
    @DisplayName("Tests getting all connectors of a certain category.")
    @SneakyThrows(Exception.class)
    void testGetConnectorsInCategory() {
        TestConnector testConnector = new TestConnector();
        when(connectorManager.loadAllOfType(eq(Set.of("category-id")), eq(666), eq(1))).thenReturn(
                new ModelPage<>(666, 1, 1, List.of(testConnector))
        );

        MvcResult mvcResult = mockMvc.perform(get("/api/connector/candidate/Y2F0ZWdvcnktaWQ=")
                .queryParam("pageNumber", "666")
                .queryParam("pageSize", "1")
        ).andExpect(status().isOk()).andReturn();

        ModelPage<TestConnector> result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(666, result.getNumber());
        assertEquals(1, result.getSize());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getItems().size());
        assertEquals(testConnector, result.getItems().get(0));
    }

    /**
     * Test getting a specific connector.
     */
    @Test
    @DisplayName("Test getting a specific connector.")
    @SneakyThrows(Exception.class)
    void testGetConnector() {
        mockMvc.perform(get("/api/connector/connector-id")).andExpect(status().isNotFound());

        TestConnector testConnector = new TestConnector();
        when(connectorManager.load(eq("connector-id"))).thenReturn(testConnector);

        MvcResult mvcResult = mockMvc.perform(get("/api/connector/connector-id")).andExpect(status().isOk()).andReturn();

        TestConnector result = convert(mvcResult, TestConnector.class);

        assertEquals(testConnector, result);
    }

    /**
     * Tests deleting a connector.
     */
    @Test
    @DisplayName("Tests deleting a connector.")
    @SneakyThrows(Exception.class)
    void testDeleteConnector() {
        // Unused Connector:
        mockMvc.perform(delete("/api/connector/connectorA-id")).andExpect(status().isNoContent());
        verify(connectorManager, times(1)).deleteConnector(eq("connectorA-id"));

        // Job using the connector is deactivated:
        when(connectorManager.getReferencingJobs(eq("connectorB-id"), eq(0), eq(Integer.MAX_VALUE))).thenReturn(
                new ModelPage<>(0, 1, 1, List.of(new Pair<>("job-id", "job-name")))
        );

        when(jobManager.load(eq("job-id"))).thenReturn(Job.builder().active(true).build());

        mockMvc.perform(delete("/api/connector/connectorB-id")).andExpect(status().isNoContent());
        verify(connectorManager, times(1)).deleteConnector(eq("connectorB-id"));

        ArgumentCaptor<Job> argCap = ArgumentCaptor.forClass(Job.class);
        verify(jobManager, times(1)).save(argCap.capture());

        assertFalse(argCap.getValue().isActive());

        // Job using the connector is deleted:
        mockMvc.perform(delete("/api/connector/connectorB-id").queryParam("deleteAffectedJobs", "true"))
                .andExpect(status().isNoContent());
        verify(connectorManager, times(2)).deleteConnector(eq("connectorB-id"));
        verify(jobManager, times(1)).delete(eq("job-id"));
    }

    /**
     * Tests creating a connector.
     */
    @Test
    @DisplayName("Tests creating a connector.")
    @SneakyThrows(Exception.class)
    void testCreateConnector() {
        when(igorComponentRegistry.createConnectorInstance(anyString(), anyMap())).thenReturn(new TestConnector());

        TestConnector testConnector = new TestConnector();
        testConnector.setId("connector-id");
        testConnector.setName("connector-name");

        when(connectorManager.save(any(Connector.class))).thenReturn(testConnector);

        MvcResult mvcResult = mockMvc.perform(post("/api/connector")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(testConnector)))
                .andExpect(status().isOk()).andReturn();

        TestConnector result = convert(mvcResult, TestConnector.class);

        assertEquals("connector-id", result.getId());
        assertEquals("connector-name", result.getName());

        verify(connectorManager, times(1)).save(eq(testConnector));

        // Connector with same name already exists:
        when(connectorManager.loadByName(anyString())).thenReturn(new TestConnector());
        mockMvc.perform(post("/api/connector")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(testConnector)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests updating a connector.
     */
    @Test
    @DisplayName("Tests updating a connector.")
    @SneakyThrows(Exception.class)
    void testUpdateConnector() {
        when(igorComponentRegistry.createConnectorInstance(anyString(), anyMap())).thenReturn(new TestConnector());

        TestConnector testConnector = new TestConnector();
        testConnector.setId("connector-id");
        testConnector.setName("update-test");

        mockMvc.perform(put("/api/connector")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(testConnector)))
                .andExpect(status().isNoContent());

        verify(connectorManager, times(1)).save(eq(testConnector));

        // Existing name, same connector:
        when(connectorManager.loadByName(eq("update-test"))).thenReturn(testConnector);
        mockMvc.perform(put("/api/connector")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(testConnector)))
                .andExpect(status().isNoContent());

        verify(connectorManager, times(2)).save(eq(testConnector));

        // Existing name, different connector:
        TestConnector existingconnector = new TestConnector();
        existingconnector.setId("existing-connector-id");
        when(connectorManager.loadByName(eq("update-test"))).thenReturn(existingconnector);

        mockMvc.perform(put("/api/connector")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(testConnector)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests testing a connector. :)
     */
    @Test
    @DisplayName("Tests testing a connector.")
    @SneakyThrows(Exception.class)
    void testTestConnector() {
        when(igorComponentRegistry.createConnectorInstance(anyString(), anyMap())).thenReturn(new TestConnector());

        mockMvc.perform(post("/api/connector/test")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(new TestConnector())))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        when(igorComponentRegistry.createConnectorInstance(anyString(), anyMap())).thenReturn(new ExceptionProvocingTestConnector());

        mockMvc.perform(post("/api/connector/test")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(new TestConnector())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("test-exception (exception-cause)"));
    }

    /**
     * Tests checking a connector's name for usage by another.
     */
    @Test
    @DisplayName("Tests checking a connector's name for usage by another.")
    @SneakyThrows(Exception.class)
    void testCheckConnectorName() {
        mockMvc.perform(get("/api/connector/check/" + Base64.getEncoder().encodeToString("connector-name".getBytes()) + "/connector-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        TestConnector testConnector = new TestConnector();
        testConnector.setId("connector-id");
        when(connectorManager.loadByName(eq("connector name"))).thenReturn(testConnector);

        // Same connector:
        mockMvc.perform(get("/api/connector/check/" + Base64.getEncoder().encodeToString("connector name".getBytes()) + "/connector-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        // Different connector with same name:
        mockMvc.perform(get("/api/connector/check/" + Base64.getEncoder().encodeToString("connector name".getBytes()) + "/other-connector-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    /**
     * Tests getting all jobs that use a certain connector.
     */
    @Test
    @DisplayName("Tests getting all jobs that use a certain connector.")
    @SneakyThrows(Exception.class)
    void testGetReferencingJobs() {
        mockMvc.perform(get("/api/connector/connector-id/job-references")).andExpect(status().isOk())
                .andExpect(content().string("{\"number\":0,\"size\":2147483647,\"totalPages\":0,\"items\":[]}"));

        when(connectorManager.getReferencingJobs(eq("connector-id"), eq(666), eq(2))).thenReturn(
                new ModelPage<>(666, 2, 1, List.of(
                        new Pair<>("job1-id", "job1-name"), new Pair<>("job2-id", "job2-name")))
        );

        MvcResult mvcResult = mockMvc.perform(get("/api/connector/connector-id/job-references")
                .queryParam("pageNumber", "666").queryParam("pageSize", "2")).andExpect(status().isOk()).andReturn();

        ModelPage<Pair<String, String>> result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(2, result.getItems().size());
        assertEquals("job1-id", result.getItems().get(0).getKey());
        assertEquals("job1-name", result.getItems().get(0).getValue());
        assertEquals("job2-id", result.getItems().get(1).getKey());
        assertEquals("job2-name", result.getItems().get(1).getValue());
    }

    /**
     * Connector for testing exceptions during connector tests.
     */
    private static class ExceptionProvocingTestConnector extends TestConnector {

        /**
         * Throws an {@link IllegalStateException} for testing.
         */
        @Override
        public void testConfiguration() {
            throw new IllegalStateException("test-exception", new IllegalStateException("exception-cause"));
        }

    }

}
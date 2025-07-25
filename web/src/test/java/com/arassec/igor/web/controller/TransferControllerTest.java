package com.arassec.igor.web.controller;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.web.model.TransferData;
import com.arassec.igor.web.test.TestConnector;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the {@link TransferController}.
 */
@DisplayName("Transfer-Controller tests")
class TransferControllerTest extends RestControllerBaseTest {

    /**
     * Tests exporting a job.
     */
    @Test
    @DisplayName("Tests exporting a job.")
    @SneakyThrows
    void testExportJob() {
        when(igorComponentUtil.getTypeId(any(IgorComponent.class))).thenReturn("type-id");
        when(igorComponentUtil.getCategoryId(any(IgorComponent.class))).thenReturn("category-id");

        mockMvc.perform(get("/api/transfer/job/job-id").contentType(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(status().isBadRequest()).andReturn();

        Job job = new Job();
        job.setId("job-id");

        when(jobManager.load("job-id")).thenReturn(job);
        when(jobManager.getReferencedConnectors("job-id")).thenReturn(Set.of(new Pair<>("connector-id", "connector-name")));
        when(connectorManager.load("connector-id")).thenReturn(new TestConnector());
        when(igorComponentRegistry.createConnectorInstance(anyString(), anyMap())).thenReturn(new TestConnector());

        MvcResult mvcResult = mockMvc.perform(get("/api/transfer/job/job-id").contentType(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(status().isOk()).andReturn();

        assertEquals("attachment; filename=job-id.igor.json", mvcResult.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(MediaType.APPLICATION_JSON_VALUE, mvcResult.getResponse().getContentType());

        TransferData transferData = convert(mvcResult, TransferData.class);

        assertEquals(1, transferData.getJobJsons().size());
        assertEquals("{\"id\":\"job-id\",\"active\":false,\"historyLimit\":5,\"simulationLimit\":25,\"numThreads\":1,"
            + "\"actions\":[],\"running\":false,\"faultTolerant\":true}",
            objectMapper.writeValueAsString(transferData.getJobJsons().getFirst()));

        assertEquals(1, transferData.getConnectorJsons().size());
        assertNotNull(transferData.getConnectorJsons().getFirst());
    }

    /**
     * Tests importing a job.
     */
    @Test
    @DisplayName("Tests importing a job.")
    @SneakyThrows
    void testImportJob() {
        when(igorComponentUtil.getTypeId(any(IgorComponent.class))).thenReturn("type-id");
        when(igorComponentUtil.getCategoryId(any(IgorComponent.class))).thenReturn("category-id");

        TestConnector testConnector = new TestConnector();
        testConnector.setId("connector-id");

        TransferData transferData = new TransferData();
        transferData.getJobJsons().add(objectMapper.convertValue(Job.builder().id("job-id").build(), new TypeReference<>() {
        }));
        transferData.getConnectorJsons().add(objectMapper.convertValue(testConnector, new TypeReference<>() {
        }));

        String content = objectMapper.writeValueAsString(transferData);

        when(igorComponentRegistry.createConnectorInstance(eq("type-id"), anyMap())).thenReturn(new TestConnector());

        mockMvc.perform(post("/api/transfer").content(content).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200)).andReturn();

        ArgumentCaptor<Connector> argCap = ArgumentCaptor.forClass(Connector.class);
        verify(connectorManager, times(1)).save(argCap.capture());
        assertEquals("connector-id", argCap.getValue().getId());

        ArgumentCaptor<Job> jobArgCap = ArgumentCaptor.forClass(Job.class);
        verify(jobManager, times(1)).save(jobArgCap.capture());
        assertEquals("job-id", jobArgCap.getValue().getId());
    }

    /**
     * Tests exporting a connector.
     */
    @Test
    @DisplayName("Tests exporting a connector.")
    @SneakyThrows
    void testExportConnector() {
        when(igorComponentUtil.getTypeId(any(IgorComponent.class))).thenReturn("type-id");
        when(igorComponentUtil.getCategoryId(any(IgorComponent.class))).thenReturn("category-id");

        mockMvc.perform(get("/api/transfer/connector/connector-id").contentType(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(status().isBadRequest()).andReturn();

        TestConnector connector = new TestConnector();
        connector.setId("connector-id");

        when(connectorManager.load("connector-id")).thenReturn(connector);

        MvcResult mvcResult = mockMvc.perform(get("/api/transfer/connector/connector-id").contentType(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(status().isOk()).andReturn();

        assertEquals("attachment; filename=connector-id.igor.json",
            mvcResult.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(MediaType.APPLICATION_JSON_VALUE, mvcResult.getResponse().getContentType());

        TransferData transferData = convert(mvcResult, TransferData.class);
        assertEquals(1, transferData.getConnectorJsons().size());

        when(igorComponentRegistry.createConnectorInstance(eq("type-id"), anyMap())).thenReturn(new TestConnector());
        Connector exportedConnector = objectMapper.convertValue(transferData.getConnectorJsons().getFirst(), Connector.class);

        assertEquals("connector-id", exportedConnector.getId());
    }

    /**
     * Tests importing a connector.
     */
    @Test
    @DisplayName("Tests importing a connector.")
    @SneakyThrows
    void testImportConnector() {
        when(igorComponentUtil.getTypeId(any(IgorComponent.class))).thenReturn("type-id");
        when(igorComponentUtil.getCategoryId(any(IgorComponent.class))).thenReturn("category-id");

        TestConnector testConnector = new TestConnector();
        testConnector.setId("connector-id");

        Map<String, Object> connectorJson = objectMapper.convertValue(testConnector, new TypeReference<>() {
        });

        TransferData transferData = new TransferData();
        transferData.getConnectorJsons().add(connectorJson);

        String content = objectMapper.writeValueAsString(transferData);

        when(igorComponentRegistry.createConnectorInstance(eq("type-id"), anyMap())).thenReturn(new TestConnector());

        mockMvc.perform(post("/api/transfer").content(content).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().is(200)).andReturn();

        verify(connectorManager, times(1)).save(any(TestConnector.class));
    }

}

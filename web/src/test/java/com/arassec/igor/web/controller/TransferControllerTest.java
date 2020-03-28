package com.arassec.igor.web.controller;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.web.model.TransferData;
import com.arassec.igor.web.test.TestService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

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
public class TransferControllerTest extends RestControllerBaseTest {

    /**
     * Tests exporting a job.
     */
    @Test
    @DisplayName("Tests exporting a job.")
    @SneakyThrows
    void testExportJob() {
        mockMvc.perform(get("/api/transfer/job/job-id").contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isBadRequest()).andReturn();

        Job job = new Job();
        job.setId("job-id");

        when(jobManager.load(eq("job-id"))).thenReturn(job);
        when(jobManager.getReferencedServices(eq("job-id"))).thenReturn(Set.of(new Pair<>("service-id", "service-name")));
        when(serviceManager.load(eq("service-id"))).thenReturn(new TestService());
        when(igorComponentRegistry.createServiceInstance(anyString(), anyMap())).thenReturn(new TestService());

        MvcResult mvcResult = mockMvc.perform(get("/api/transfer/job/job-id").contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk()).andReturn();

        assertEquals("attachment; filename=job-id.igor.json", mvcResult.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(MediaType.APPLICATION_JSON_VALUE, mvcResult.getResponse().getContentType());

        TransferData transferData = convert(mvcResult, TransferData.class);

        assertEquals(1, transferData.getJobJsons().size());
        assertEquals("{\"id\":\"job-id\",\"active\":false,\"executionHistoryLimit\":5,\"tasks\":[],\"running\":false," +
                "\"faultTolerant\":true}", transferData.getJobJsons().get(0));

        assertEquals(1, transferData.getServiceJsons().size());
        assertNotNull(transferData.getServiceJsons().get(0));
    }

    /**
     * Tests importing a job.
     */
    @Test
    @DisplayName("Tests importing a job.")
    @SneakyThrows
    void testImportJob() {
        TestService testService = new TestService();
        testService.setId("service-id");

        TransferData transferData = new TransferData();
        transferData.getJobJsons().add(objectMapper.writeValueAsString(Job.builder().id("job-id").build()));
        transferData.getServiceJsons().add(objectMapper.writeValueAsString(testService));

        String content = objectMapper.writeValueAsString(transferData);

        when(igorComponentRegistry.createServiceInstance(eq(testService.getTypeId()), anyMap())).thenReturn(new TestService());

        mockMvc.perform(post("/api/transfer").content(content).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(200)).andReturn();

        ArgumentCaptor<Service> argCap = ArgumentCaptor.forClass(Service.class);
        verify(serviceManager, times(1)).save(argCap.capture());
        assertEquals("service-id", argCap.getValue().getId());

        ArgumentCaptor<Job> jobArgCap = ArgumentCaptor.forClass(Job.class);
        verify(jobManager, times(1)).save(jobArgCap.capture());
        assertEquals("job-id", jobArgCap.getValue().getId());
    }

    /**
     * Tests exporting a service.
     */
    @Test
    @DisplayName("Tests exporting a service.")
    @SneakyThrows
    void testExportService() {
        mockMvc.perform(get("/api/transfer/service/service-id").contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isBadRequest()).andReturn();

        TestService service = new TestService();
        service.setId("service-id");

        when(serviceManager.load(eq("service-id"))).thenReturn(service);

        MvcResult mvcResult = mockMvc.perform(get("/api/transfer/service/service-id").contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(status().isOk()).andReturn();

        assertEquals("attachment; filename=service-id.igor.json",
                mvcResult.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(MediaType.APPLICATION_JSON_VALUE, mvcResult.getResponse().getContentType());

        TransferData transferData = convert(mvcResult, TransferData.class);
        assertEquals(1, transferData.getServiceJsons().size());

        when(igorComponentRegistry.createServiceInstance(eq("service-type-id"), anyMap())).thenReturn(new TestService());
        Service exportedService = objectMapper.readValue( transferData.getServiceJsons().get(0), Service.class);

        assertEquals("service-id", exportedService.getId());
    }

    /**
     * Tests importing a service.
     */
    @Test
    @DisplayName("Tests importing a service.")
    @SneakyThrows
    void testImportService() {
        TestService testService = new TestService();
        testService.setId("service-id");

        String serviceJson = objectMapper.writeValueAsString(testService);

        TransferData transferData = new TransferData();
        transferData.getServiceJsons().add(serviceJson);

        String content = objectMapper.writeValueAsString(transferData);

        when(igorComponentRegistry.createServiceInstance(eq(testService.getTypeId()), anyMap())).thenReturn(new TestService());

        mockMvc.perform(post("/api/transfer").content(content).contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(200)).andReturn();

        verify(serviceManager, times(1)).save(any(TestService.class));
    }

}

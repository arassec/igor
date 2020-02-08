package com.arassec.igor.web.controller;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.web.model.ServiceListEntry;
import com.arassec.igor.web.test.TestService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the {@link ServiceRestController}.
 */
@DisplayName("Service-Controller Tests")
class ServiceRestControllerTest extends BaseRestControllerTest {

    /**
     * Tests getting Services.
     */
    @Test
    @DisplayName("Tests getting Services.")
    @SneakyThrows(Exception.class)
    void getServices() {
        MvcResult mvcResult = mockMvc.perform(get("/api/service")).andExpect(status().isOk()).andReturn();

        ModelPage<ServiceListEntry> result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(0, result.getNumber());
        assertEquals(Integer.MAX_VALUE, result.getSize());
        assertEquals(0, result.getTotalPages());
        assertNotNull(result.getItems());

        TestService testService = new TestService();
        testService.setId("service-id");
        testService.setName("service-name");
        when(serviceManager.loadPage(eq(666), eq(2), eq("name-filter"))).thenReturn(
                new ModelPage<>(666, 2, 1, List.of(testService)));

        mvcResult = mockMvc.perform(get("/api/service")
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

        ServiceListEntry serviceListEntry = result.getItems().get(0);

        assertEquals("service-id", serviceListEntry.getId());
        assertEquals("service-name", serviceListEntry.getName());
    }

    /**
     * Tests getting all services of a certain category.
     */
    @Test
    @DisplayName("Tests getting all services of a certain category.")
    @SneakyThrows(Exception.class)
    void getServicesInCategory() {
        TestService testService = new TestService();
        when(serviceManager.loadAllOfCategory(eq("category-id"), eq(666), eq(1))).thenReturn(
                new ModelPage<>(666, 1, 1, List.of(testService))
        );

        MvcResult mvcResult = mockMvc.perform(get("/api/service/category/category-id")
                .queryParam("pageNumber", "666")
                .queryParam("pageSize", "1")
        ).andExpect(status().isOk()).andReturn();

        ModelPage<TestService> result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(666, result.getNumber());
        assertEquals(1, result.getSize());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getItems().size());
        assertEquals(testService, result.getItems().get(0));
    }

    /**
     * Test getting a specific service.
     */
    @Test
    @DisplayName("Test getting a specific service.")
    @SneakyThrows(Exception.class)
    void getService() {
        mockMvc.perform(get("/api/service/service-id")).andExpect(status().isNotFound());

        TestService testService = new TestService();
        when(serviceManager.load(eq("service-id"))).thenReturn(testService);

        MvcResult mvcResult = mockMvc.perform(get("/api/service/service-id")).andExpect(status().isOk()).andReturn();

        TestService result = convert(mvcResult, TestService.class);

        assertEquals(testService, result);
    }

    /**
     * Tests deleting a service.
     */
    @Test
    @DisplayName("Tests deleting a service.")
    @SneakyThrows(Exception.class)
    void deleteService() {
        // Unused Service:
        mockMvc.perform(delete("/api/service/serviceA-id")).andExpect(status().isNoContent());
        verify(serviceManager, times(1)).deleteService(eq("serviceA-id"));

        // Job using the service is deactivated:
        when(serviceManager.getReferencingJobs(eq("serviceB-id"), eq(0), eq(Integer.MAX_VALUE))).thenReturn(
                new ModelPage<>(0, 1, 1, List.of(new Pair<>("job-id", "job-name")))
        );

        when(jobManager.load(eq("job-id"))).thenReturn(Job.builder().active(true).build());

        mockMvc.perform(delete("/api/service/serviceB-id")).andExpect(status().isNoContent());
        verify(serviceManager, times(1)).deleteService(eq("serviceB-id"));

        ArgumentCaptor<Job> argCap = ArgumentCaptor.forClass(Job.class);
        verify(jobManager, times(1)).save(argCap.capture());

        assertFalse(argCap.getValue().isActive());

        // Job using the service is deleted:
        mockMvc.perform(delete("/api/service/serviceB-id").queryParam("deleteAffectedJobs", "true"))
                .andExpect(status().isNoContent());
        verify(serviceManager, times(2)).deleteService(eq("serviceB-id"));
        verify(jobManager, times(1)).delete(eq("job-id"));
    }

    /**
     * Tests creating a service.
     */
    @Test
    @DisplayName("Tests creating a service.")
    @SneakyThrows(Exception.class)
    void createService() {
        when(igorComponentRegistry.createServiceInstance(anyString(), anyMap())).thenReturn(new TestService());

        TestService testService = new TestService();
        testService.setId("service-id");
        testService.setName("service-name");

        when(serviceManager.save(any(Service.class))).thenReturn(testService);

        MvcResult mvcResult = mockMvc.perform(post("/api/service")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(testService)))
                .andExpect(status().isOk()).andReturn();

        TestService result = convert(mvcResult, TestService.class);

        assertEquals("service-id", result.getId());
        assertEquals("service-name", result.getName());

        verify(serviceManager, times(1)).save(eq(testService));

        // Service with same name already exists:
        when(serviceManager.loadByName(anyString())).thenReturn(new TestService());
        mockMvc.perform(post("/api/service")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(testService)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests updating a service.
     */
    @Test
    @DisplayName("Tests updating a service.")
    @SneakyThrows(Exception.class)
    void updateService() {
        when(igorComponentRegistry.createServiceInstance(anyString(), anyMap())).thenReturn(new TestService());

        TestService testService = new TestService();
        testService.setId("service-id");
        testService.setName("update-test");

        mockMvc.perform(put("/api/service")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(testService)))
                .andExpect(status().isNoContent());

        verify(serviceManager, times(1)).save(eq(testService));

        // Existing name, same service:
        when(serviceManager.loadByName(eq("update-test"))).thenReturn(testService);
        mockMvc.perform(put("/api/service")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(testService)))
                .andExpect(status().isNoContent());

        verify(serviceManager, times(2)).save(eq(testService));

        // Existing name, different service:
        TestService existingService = new TestService();
        existingService.setId("existing-service-id");
        when(serviceManager.loadByName(eq("update-test"))).thenReturn(existingService);

        mockMvc.perform(put("/api/service")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(testService)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests testing a service. :)
     */
    @Test
    @DisplayName("Tests testing a service.")
    @SneakyThrows(Exception.class)
    void testService() {
        when(igorComponentRegistry.createServiceInstance(anyString(), anyMap())).thenReturn(new TestService());

        mockMvc.perform(post("/api/service/test")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(new TestService())))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        when(igorComponentRegistry.createServiceInstance(anyString(), anyMap())).thenReturn(new ExceptionProvocingTestService());

        mockMvc.perform(post("/api/service/test")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(new TestService())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("test-exception (exception-cause)"));
    }

    /**
     * Tests checking a service's name for usage by another.
     */
    @Test
    @DisplayName("Tests checking a service's name for usage by another.")
    @SneakyThrows(Exception.class)
    void checkServiceName() {
        mockMvc.perform(get("/api/service/check/" + Base64.getEncoder().encodeToString("service-name".getBytes()) + "/service-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        TestService testService = new TestService();
        testService.setId("service-id");
        when(serviceManager.loadByName(eq("service name"))).thenReturn(testService);

        // Same service:
        mockMvc.perform(get("/api/service/check/" + Base64.getEncoder().encodeToString("service name".getBytes()) + "/service-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        // Different service with same name:
        mockMvc.perform(get("/api/service/check/" + Base64.getEncoder().encodeToString("service name".getBytes()) + "/other-service-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    /**
     * Tests getting all jobs that use a certain service.
     */
    @Test
    @DisplayName("Tests getting all jobs that use a certain service.")
    @SneakyThrows(Exception.class)
    void getReferencingJobs() {
        mockMvc.perform(get("/api/service/service-id/job-references")).andExpect(status().isOk())
                .andExpect(content().string("{\"number\":0,\"size\":2147483647,\"totalPages\":0,\"items\":[]}"));

        when(serviceManager.getReferencingJobs(eq("service-id"), eq(666), eq(2))).thenReturn(
                new ModelPage<>(666, 2, 1, List.of(
                        new Pair<>("job1-id", "job1-name"), new Pair<>("job2-id", "job2-name")))
        );

        MvcResult mvcResult = mockMvc.perform(get("/api/service/service-id/job-references")
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
     * Service for testing exceptions during service tests.
     */
    private static class ExceptionProvocingTestService extends TestService {

        /**
         * Throws an {@link IllegalStateException} for testing.
         */
        @Override
        public void testConfiguration() {
            throw new IllegalStateException("test-exception", new IllegalStateException("exception-cause"));
        }
    }

}
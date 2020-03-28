package com.arassec.igor.web.controller;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.Task;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.web.model.JobListEntry;
import com.arassec.igor.web.model.ScheduleEntry;
import com.arassec.igor.web.model.simulation.SimulationJobResult;
import com.arassec.igor.web.simulation.ActionProxy;
import com.arassec.igor.web.simulation.ProviderProxy;
import com.arassec.igor.web.test.TestTrigger;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests the {@link JobRestController}.
 */
@DisplayName("Job-Controller Tests")
class JobRestControllerTest extends RestControllerBaseTest {

    /**
     * Tests reading job list entries.
     */
    @Test
    @DisplayName("Tests reading job list entries.")
    @SneakyThrows
    void testGetJobs() {
        mockMvc.perform(get("/api/job")).andExpect(status().isOk())
                .andExpect(content().string("{\"number\":0,\"size\":2147483647,\"totalPages\":0,\"items\":[]}"));

        when(jobManager.loadPage(eq(1), eq(2), eq("name-filter"), anySet())).thenReturn(
                new ModelPage<>(1, 2, 3, List.of(
                        Job.builder().id("job-id").name("job-name").active(true).build()
                )));

        MvcResult mvcResult = mockMvc.perform(get("/api/job")
                .queryParam("pageNumber", "1")
                .queryParam("pageSize", "2")
                .queryParam("nameFilter", "name-filter"))
                .andExpect(status().isOk()).andReturn();

        ModelPage<JobListEntry> result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(1, result.getNumber());
        assertEquals(2, result.getSize());

        assertEquals(1, result.getItems().size());
        assertEquals("job-id", result.getItems().get(0).getId());
        assertEquals("job-name", result.getItems().get(0).getName());
        assertTrue(result.getItems().get(0).isActive());
    }

    /**
     * Tests reading job list entries with state filter.
     */
    @Test
    @DisplayName("Tests reading job list entries with state filter.")
    @SneakyThrows
    void testGetJobsWithStateFilter() {
        mockMvc.perform(get("/api/job")).andExpect(status().isOk())
                .andExpect(content().string("{\"number\":0,\"size\":2147483647,\"totalPages\":0,\"items\":[]}"));

        when(jobManager.loadPage(eq(1), eq(2), eq("name-filter"), anySet())).thenReturn(
                new ModelPage<>(1, 2, 3, List.of(
                        Job.builder().id("job-id").name("job-name").currentJobExecution(
                                JobExecution.builder().executionState(JobExecutionState.RUNNING).id(123L).build()
                        ).active(true).build()
                )));

        mockMvc.perform(get("/api/job")
                .queryParam("pageNumber", "1")
                .queryParam("pageSize", "2")
                .queryParam("nameFilter", "name-filter")
                .queryParam("stateFilter", JobExecutionState.RUNNING.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].execution.id").value(123L))
                .andExpect(jsonPath("$.items[0].execution.state").value(JobExecutionState.RUNNING.name()))
                .andExpect(jsonPath("$.items[0].execution.duration").value(""));
    }

    /**
     * Tests reading a job.
     */
    @Test
    @DisplayName("Tests reading a job.")
    @SneakyThrows
    void testGetJob() {
        mockMvc.perform(get("/api/job/job-id")).andExpect(status().isNotFound());

        when(jobManager.load(eq("job-id"))).thenReturn(Job.builder().id("job-id").name("job-name").build());

        MvcResult mvcResult = mockMvc.perform(get("/api/job/job-id")).andExpect(status().isOk()).andReturn();

        Job result = convert(mvcResult, Job.class);

        assertEquals("job-id", result.getId());
        assertEquals("job-name", result.getName());
    }

    /**
     * Tests creating a job.
     */
    @Test
    @DisplayName("Tests creating a job.")
    @SneakyThrows
    void testCreateJob() {
        when(jobManager.save(any(Job.class))).thenReturn(Job.builder().id("job-id").name("saved-job").build());

        MvcResult mvcResult = mockMvc.perform(post("/api/job")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(Job.builder().name("job-name").build())))
                .andExpect(status().isOk()).andReturn();

        Job result = convert(mvcResult, Job.class);

        assertEquals("job-id", result.getId());
        assertEquals("saved-job", result.getName());

        // Job name is already taken:
        when(jobManager.loadByName(eq("job-name"))).thenReturn(Job.builder().build());

        mockMvc.perform(post("/api/job")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(Job.builder().name("job-name").build())))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests updating a job.
     */
    @Test
    @DisplayName("Tests updating a job.")
    @SneakyThrows
    void testUpdateJob() {
        when(jobManager.save(any(Job.class))).thenReturn(Job.builder().id("job-id").name("saved-job").build());

        MvcResult mvcResult = mockMvc.perform(put("/api/job")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(Job.builder().id("job-id").name("job-name").build())))
                .andExpect(status().isOk()).andReturn();

        Job result = convert(mvcResult, Job.class);

        assertEquals("job-id", result.getId());
        assertEquals("saved-job", result.getName());

        // Job name is already taken by another job:
        when(jobManager.loadByName(eq("job-name"))).thenReturn(Job.builder().id("job-id").build());

        mockMvc.perform(put("/api/job")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(Job.builder().name("job-name").build())))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests simulating a job execution.
     */
    @Test
    @DisplayName("Tests simulating a job execution.")
    @SneakyThrows
    void testSimulateJob() {
        Job jobSpy = spy(Job.class);
        when(jobSpy.getId()).thenReturn("job-id");

        ProviderProxy providerProxyMock = mock(ProviderProxy.class);
        when(providerProxyMock.getErrorCause()).thenReturn("provider-error-cause");
        when(providerProxyMock.getCollectedData()).thenReturn(List.of(Map.of("provider-key", "provider-value")));

        ActionProxy actionProxyMock = mock(ActionProxy.class);
        when(actionProxyMock.getErrorCause()).thenReturn("action-error-cause");
        when(actionProxyMock.getCollectedData()).thenReturn(List.of(Map.of("action-key", "action-value")));

        when(jobSpy.getTasks()).thenReturn(List.of(
                Task.builder().id("task-id").provider(providerProxyMock).actions(List.of(actionProxyMock)).build()));

        when(simulationObjectMapper.readValue(anyString(), eq(Job.class))).thenReturn(jobSpy);

        doAnswer(invocationOnMock -> {
            JobExecution jobExecution = invocationOnMock.getArgument(0);
            jobExecution.setErrorCause("job-error-cause");
            return null;
        }).when(jobSpy).run(any(JobExecution.class));

        MvcResult mvcResult = mockMvc.perform(post("/api/job/simulate")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(Job.builder().id("job-id").build())))
                .andExpect(status().isOk()).andReturn();

        SimulationJobResult result = convert(mvcResult, SimulationJobResult.class);

        assertEquals("job-error-cause", result.getErrorCause());
        assertEquals("provider-error-cause", result.getTaskResults().get(0).getErrorCause());
        assertEquals("action-error-cause", result.getTaskResults().get(0).getActionResults().get(0).getErrorCause());

        Map<String, Object> providerData = result.getTaskResults().get(0).getResults().get(0);
        assertTrue(providerData.containsKey(DataKey.DATA.getKey()));
        assertTrue(providerData.containsKey(DataKey.META.getKey()));

        @SuppressWarnings("unchecked")
        Map<String, Object> providerDataItem = (Map<String, Object>) providerData.get(DataKey.DATA.getKey());
        assertEquals("provider-value", providerDataItem.get("provider-key"));

        Map<String, Object> actionDataItem = result.getTaskResults().get(0).getActionResults().get(0).getResults().get(0);
        assertEquals("action-value", actionDataItem.get("action-key"));
    }

    /**
     * Tests checking a job name for already existing jobs with the same name.
     */
    @Test
    @DisplayName("Tests checking a job name for already existing jobs with the same name.")
    @SneakyThrows
    void testCheckJobName() {
        mockMvc.perform(get("/api/job/check/" + Base64.getEncoder().encodeToString("job-name".getBytes()) + "/job-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        when(jobManager.loadByName(eq("job name"))).thenReturn(Job.builder().id("job-id").build());

        // Same job:
        mockMvc.perform(get("/api/job/check/" + Base64.getEncoder().encodeToString("job name".getBytes()) + "/job-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        // Different job with same name:
        mockMvc.perform(get("/api/job/check/" + Base64.getEncoder().encodeToString("job name".getBytes()) + "/other-job-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    /**
     * Tests deleting a job.
     */
    @Test
    @DisplayName("Tests deleting a job.")
    @SneakyThrows
    void testDeleteJob() {
        mockMvc.perform(delete("/api/job/job-id").queryParam("deleteExclusiveServices", "false"))
                .andExpect(status().isNoContent());
        verify(jobManager, times(1)).delete(eq("job-id"));

        when(jobManager.getReferencedServices(eq("other-job-id"))).thenReturn(Set.of(new Pair<>("single-ref-id",
                "single-ref-service"), new Pair<>("multi-ref-id", "multi-ref-service")));

        when(serviceManager.getReferencingJobs(eq("single-ref-id"), eq(0), eq(Integer.MAX_VALUE))).thenReturn(
                new ModelPage<>(0, 1, 1, List.of(new Pair<>("other-job-id", "other-job-name")))
        );
        when(serviceManager.getReferencingJobs(eq("multi-ref-id"), eq(0), eq(Integer.MAX_VALUE))).thenReturn(
                new ModelPage<>(0, 1, 1, List.of(new Pair<>("other-job-id", "other-job-name"),
                        new Pair<>("yet-another-job-id", "yet-another-job-name")))
        );

        mockMvc.perform(delete("/api/job/other-job-id").queryParam("deleteExclusiveServices", "true"))
                .andExpect(status().isNoContent());

        verify(serviceManager, times(1)).deleteService(eq("single-ref-id"));

        verify(jobManager, times(1)).delete(eq("other-job-id"));
    }

    /**
     * Tests manually starting a job.
     */
    @Test
    @DisplayName("Tests manually starting a job.")
    @SneakyThrows
    void testRunJob() {
        Job savedJob = Job.builder().name("saved-job-name").active(false).build();
        when(jobManager.save(any(Job.class))).thenReturn(savedJob);

        MvcResult mvcResult = mockMvc.perform(post("/api/job/run")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(Job.builder().name("job-name").build())))
                .andExpect(status().isOk()).andReturn();
        Job result = convert(mvcResult, Job.class);
        assertEquals(savedJob, result);
        verify(jobManager, times(0)).enqueue(eq(savedJob));

        savedJob.setActive(true);
        mockMvc.perform(post("/api/job/run")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(Job.builder().name("job-name").build())))
                .andExpect(status().isOk());
        verify(jobManager, times(1)).enqueue(eq(savedJob));
    }

    /**
     * Tests running a job by ID.
     */
    @Test
    @DisplayName("Tests running a job by ID.")
    @SneakyThrows
    void testRunJobFromId() {
        mockMvc.perform(post("/api/job/run/job-id"))
                .andExpect(status().isBadRequest());

        Job job = Job.builder().active(true).build();
        when(jobManager.load(eq("job-id"))).thenReturn(job);

        mockMvc.perform(post("/api/job/run/job-id"))
                .andExpect(status().isNoContent());

        verify(jobManager, times(1)).enqueue(eq(job));
    }

    /**
     * Tests retrieving the current job schedule.
     */
    @Test
    @DisplayName("Tests retrieving the current job schedule.")
    @SneakyThrows
    void testGetSchedule() {
        when(jobManager.loadScheduled()).thenReturn(List.of(
                Job.builder().id("id1").name("name1").trigger(new TestTrigger()).build(),
                Job.builder().id("id2").name("name2").build(),
                Job.builder().id("id3").name("name3").trigger(new TestTrigger()).build()
        ));

        MvcResult mvcResult = mockMvc.perform(get("/api/job/schedule")).andExpect(status().isOk()).andReturn();

        ModelPage<ScheduleEntry> result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(2, result.getItems().size());
        assertEquals("id1", result.getItems().get(0).getJobId());
        assertEquals("name1", result.getItems().get(0).getJobName());
        assertEquals("id3", result.getItems().get(1).getJobId());
        assertEquals("name3", result.getItems().get(1).getJobName());
    }

    /**
     * Tests getting exclusive services of a job.
     */
    @Test
    @DisplayName("Tests getting exclusive services of a job.")
    @SneakyThrows
    void testGetExclusiveServices() {
        when(jobManager.getReferencedServices(eq("test-job-id"))).thenReturn(Set.of(
                new Pair<>("serviceC-id", "serviceC"),
                new Pair<>("serviceB-id", "serviceB"),
                new Pair<>("serviceA-id", "serviceA")
        ));

        when(serviceManager.getReferencingJobs(eq("serviceA-id"), eq(0), eq(Integer.MAX_VALUE))).thenReturn(
                new ModelPage<>(0, 1, 1, List.of(new Pair<>("test-job-id", "test-job-name")))
        );
        when(serviceManager.getReferencingJobs(eq("serviceB-id"), eq(0), eq(Integer.MAX_VALUE))).thenReturn(
                new ModelPage<>(0, 1, 1, List.of(new Pair<>("test-job-id", "test-job-name"),
                        new Pair<>("another-job-id", "another-job-name")))
        );
        when(serviceManager.getReferencingJobs(eq("serviceC-id"), eq(0), eq(Integer.MAX_VALUE))).thenReturn(
                new ModelPage<>(0, 1, 1, List.of(new Pair<>("test-job-id", "test-job-name")))
        );

        MvcResult mvcResult = mockMvc.perform(get("/api/job/test-job-id/exclusive-service-references"))
                .andExpect(status().isOk())
                .andReturn();

        List<Pair<String, String>> result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(2, result.size());
        assertEquals("serviceA-id", result.get(0).getKey());
        assertEquals("serviceA", result.get(0).getValue());
        assertEquals("serviceC-id", result.get(1).getKey());
        assertEquals("serviceC", result.get(1).getValue());
    }

}
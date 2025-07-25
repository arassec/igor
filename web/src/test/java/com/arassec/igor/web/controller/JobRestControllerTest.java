package com.arassec.igor.web.controller;

import com.arassec.igor.application.simulation.SimulationResult;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.core.util.event.JobEvent;
import com.arassec.igor.core.util.event.JobEventType;
import com.arassec.igor.web.model.JobExecutionOverview;
import com.arassec.igor.web.model.JobListEntry;
import com.arassec.igor.web.model.ScheduleEntry;
import com.arassec.igor.web.test.TestAction;
import com.arassec.igor.web.test.TestTrigger;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
     * The REST controller for job handling.
     */
    @Autowired
    private JobRestController jobRestController;

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

        when(jobManager.countExecutionsOfJobInState("job-id", JobExecutionState.FAILED)).thenReturn(3);

        mockMvc.perform(get("/api/job")
                .queryParam("pageNumber", "1")
                .queryParam("pageSize", "2")
                .queryParam("nameFilter", "name-filter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("1"))
                .andExpect(jsonPath("$.size").value("2"))
                .andExpect(jsonPath("$.totalPages").value("3"))
                .andExpect(jsonPath("$.items[0].id").value("job-id"))
                .andExpect(jsonPath("$.items[0].name").value("job-name"))
                .andExpect(jsonPath("$.items[0].active").value("true"))
                .andExpect(jsonPath("$.items[0].faultTolerant").value("true"))
                .andExpect(jsonPath("$.items[0].hasFailedExecutions").value("true"))
                .andExpect(jsonPath("$.items[1]").doesNotExist());
    }

    /**
     * Tests creating a job prototype instance.
     */
    @Test
    @DisplayName("Tests creating a job prototype instance")
    @SneakyThrows
    void testGetJobPrototype() {
        when(igorComponentRegistry.createJobPrototype()).thenReturn(Job.builder().id("job-id").name("new-job").build());

        mockMvc.perform(get("/api/job/prototype"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("job-id"))
                .andExpect(jsonPath("$.name").value("new-job"));
    }

    /**
     * Tests creating an action prototype instance.
     */
    @Test
    @DisplayName("Tests creating an action prototype instance")
    @SneakyThrows
    void testGetActionPrototype() {
        TestAction testAction = new TestAction();
        testAction.setId("action-id");

        when(igorComponentUtil.getTypeId(any(TestAction.class))).thenReturn("type-id");
        when(igorComponentUtil.getCategoryId(any(TestAction.class))).thenReturn("category-id");

        when(igorComponentRegistry.createActionPrototype()).thenReturn(testAction);

        mockMvc.perform(get("/api/job/action/prototype"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("action-id"));
    }

    /**
     * Tests getting a stream of job data.
     */
    @Test
    @DisplayName("Tests getting a stream of job data.")
    @SneakyThrows
    void testGetJobStream() {
        MvcResult mvcResult = mockMvc.perform(get("/api/job/stream")).andExpect(status().isOk()).andReturn();
        String streamContent = mvcResult.getResponse().getContentAsString();
        assertEquals("event:execution-overview\ndata:{\"numSlots\":0,\"numRunning\":0,\"numWaiting\":0,\"numFailed\":0}\n\n",
                streamContent);
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

        when(jobManager.load("job-id")).thenReturn(Job.builder().id("job-id").name("job-name").build());

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
                .content(objectMapper.writeValueAsString(Job.builder().id("job-id").name("job-name").build())))
                .andExpect(status().isOk()).andReturn();

        Job result = convert(mvcResult, Job.class);

        assertEquals("job-id", result.getId());
        assertEquals("saved-job", result.getName());

        // Job name is already taken:
        when(jobManager.loadByName("job-name")).thenReturn(Job.builder().build());

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

        MvcResult mvcResult = mockMvc.perform(post("/api/job")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(Job.builder().id("job-id").name("job-name").build())))
                .andExpect(status().isOk()).andReturn();

        Job result = convert(mvcResult, Job.class);

        assertEquals("job-id", result.getId());
        assertEquals("saved-job", result.getName());

        // Job name is already taken by another job:
        when(jobManager.loadByName("job-name")).thenReturn(Job.builder().id("job-id").build());

        mockMvc.perform(post("/api/job")
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
        Job job = Job.builder().id("job-id").name("job-name").build();

        @SuppressWarnings("unchecked")
        Future<Map<String, SimulationResult>> futureMock = mock(Future.class);
        when(futureMock.get()).thenReturn(Map.of("a", new SimulationResult(List.of(), "error-cause")));

        when(jobSimulator.simulateJob(job)).thenReturn(futureMock);

        mockMvc.perform(post("/api/job/simulate")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.a.errorCause").value("error-cause"));
    }

    /**
     * Tests cancelling all simulations of a job.
     */
    @Test
    @DisplayName("Tests cancelling all simulations of a job.")
    @SneakyThrows
    void testCancelSimulations() {
        mockMvc.perform(delete("/api/job/simulate/job-id")).andExpect(status().isOk());
        verify(jobSimulator, times(1)).cancelAllSimulations("job-id");
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

        when(jobManager.loadByName("job name")).thenReturn(Job.builder().id("job-id").build());

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
        mockMvc.perform(delete("/api/job/job-id").queryParam("deleteExclusiveConnectors", "false"))
                .andExpect(status().isNoContent());
        verify(jobManager, times(1)).delete("job-id");

        when(jobManager.getReferencedConnectors("other-job-id")).thenReturn(Set.of(new Pair<>("single-ref-id",
                "single-ref-connector"), new Pair<>("multi-ref-id", "multi-ref-connector")));

        when(connectorManager.getReferencingJobs("single-ref-id", 0, Integer.MAX_VALUE)).thenReturn(
                new ModelPage<>(0, 1, 1, List.of(new Pair<>("other-job-id", "other-job-name")))
        );
        when(connectorManager.getReferencingJobs("multi-ref-id", 0, Integer.MAX_VALUE)).thenReturn(
                new ModelPage<>(0, 1, 1, List.of(new Pair<>("other-job-id", "other-job-name"),
                        new Pair<>("yet-another-job-id", "yet-another-job-name")))
        );

        mockMvc.perform(delete("/api/job/other-job-id").queryParam("deleteExclusiveConnectors", "true"))
                .andExpect(status().isNoContent());

        verify(connectorManager, times(1)).deleteConnector("single-ref-id");

        verify(jobManager, times(1)).delete("other-job-id");
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
                .content(objectMapper.writeValueAsString(Job.builder().id("job-id").name("job-name").build())))
                .andExpect(status().isOk()).andReturn();
        Job result = convert(mvcResult, Job.class);
        assertEquals(savedJob, result);
        verify(jobManager, times(0)).enqueue(savedJob);

        savedJob.setActive(true);
        mockMvc.perform(post("/api/job/run")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(Job.builder().id("job-id").name("job-name").build())))
                .andExpect(status().isOk());
        verify(jobManager, times(1)).enqueue(savedJob);
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
        when(jobManager.load("job-id")).thenReturn(job);

        mockMvc.perform(post("/api/job/run/job-id"))
                .andExpect(status().isNoContent());

        verify(jobManager, times(1)).enqueue(job);
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
        assertEquals("id1", result.getItems().getFirst().getJobId());
        assertEquals("name1", result.getItems().getFirst().getJobName());
        assertEquals("id3", result.getItems().get(1).getJobId());
        assertEquals("name3", result.getItems().get(1).getJobName());
    }

    /**
     * Tests getting exclusive connectors of a job.
     */
    @Test
    @DisplayName("Tests getting exclusive connectors of a job.")
    @SneakyThrows
    void testGetExclusiveconnectors() {
        when(jobManager.getReferencedConnectors("test-job-id")).thenReturn(Set.of(
                new Pair<>("connectorC-id", "connectorC"),
                new Pair<>("connectorB-id", "connectorB"),
                new Pair<>("connectorA-id", "connectorA")
        ));

        when(connectorManager.getReferencingJobs("connectorA-id", 0, Integer.MAX_VALUE)).thenReturn(
                new ModelPage<>(0, 1, 1, List.of(new Pair<>("test-job-id", "test-job-name")))
        );
        when(connectorManager.getReferencingJobs("connectorB-id", 0, Integer.MAX_VALUE)).thenReturn(
                new ModelPage<>(0, 1, 1, List.of(new Pair<>("test-job-id", "test-job-name"),
                        new Pair<>("another-job-id", "another-job-name")))
        );
        when(connectorManager.getReferencingJobs("connectorC-id", 0, Integer.MAX_VALUE)).thenReturn(
                new ModelPage<>(0, 1, 1, List.of(new Pair<>("test-job-id", "test-job-name")))
        );

        MvcResult mvcResult = mockMvc.perform(get("/api/job/test-job-id/exclusive-connector-references"))
                .andExpect(status().isOk())
                .andReturn();

        List<Pair<String, String>> result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(2, result.size());
        assertEquals("connectorA-id", result.getFirst().getKey());
        assertEquals("connectorA", result.getFirst().getValue());
        assertEquals("connectorC-id", result.get(1).getKey());
        assertEquals("connectorC", result.get(1).getValue());
    }

    /**
     * Tests bean validation on job creation. Nested validation of components is tested, too.
     */
    @Test
    @DisplayName("Tests bean validation on job creation.")
    @SneakyThrows
    @SuppressWarnings("unchecked")
    void testCreateJobBeanValidation() {

        TestTrigger trigger = new TestTrigger();
        trigger.setId("trigger-id");
        trigger.setTestParam(null); // Notnull parameter must be validated!

        when(igorComponentUtil.getTypeId(trigger)).thenReturn("trigger-type-id");
        when(igorComponentUtil.getCategoryId(trigger)).thenReturn("trigger-category-id");

        when(igorComponentRegistry.createTriggerInstance(eq("trigger-type-id"), any(Map.class))).thenReturn(trigger);

        Job job = Job.builder().id("job-id").name("job-name").trigger(trigger).build();

        mockMvc.perform(post("/api/job")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.trigger-id.testParam").value("must not be null"));
    }

    /**
     * Tests handling job state refresh events.
     */
    @Test
    @DisplayName("Tests handling job state refresh events.")
    @SneakyThrows
    void testOnJobEventStateRefresh() {
        SseEmitter emitterMock = mock(SseEmitter.class);

        JobExecution jobExecution = JobExecution.builder().id(42L).executionState(JobExecutionState.FINISHED).build();

        //noinspection unchecked
        ((CopyOnWriteArrayList<SseEmitter>) Objects.requireNonNull(ReflectionTestUtils.getField(jobRestController,
                "jobStreamEmitters"))).add(emitterMock);

        jobRestController.onJobEvent(
                new JobEvent(JobEventType.STATE_REFRESH,
                        Job.builder().id("job-id").name("job-name").currentJobExecution(jobExecution).build())
        );

        ArgumentCaptor<SseEmitter.SseEventBuilder> argCap = ArgumentCaptor.forClass(SseEmitter.SseEventBuilder.class);
        verify(emitterMock, times(1)).send(argCap.capture());

        Iterator<ResponseBodyEmitter.DataWithMediaType> eventPartsIterator = argCap.getValue().build().iterator();

        ResponseBodyEmitter.DataWithMediaType eventPart = eventPartsIterator.next();
        assertEquals("event:state-update\ndata:", eventPart.getData());

        eventPart = eventPartsIterator.next();
        JobListEntry jobListEntry = (JobListEntry) eventPart.getData();
        assertEquals("job-id", jobListEntry.getId());
        assertEquals("job-name", jobListEntry.getName());
        assertEquals(42L, jobListEntry.getExecution().getId());
        assertEquals(JobExecutionState.FINISHED.name(), jobListEntry.getExecution().getState());
    }

    /**
     * Tests handling job state change events.
     */
    @Test
    @DisplayName("Tests handling job state change events.")
    @SneakyThrows
    void testOnJobEventStateChange() {
        SseEmitter emitterMock = mock(SseEmitter.class);

        JobExecution jobExecution = JobExecution.builder().id(42L).executionState(JobExecutionState.FINISHED).build();

        //noinspection unchecked
        ((CopyOnWriteArrayList<SseEmitter>) Objects.requireNonNull(ReflectionTestUtils.getField(jobRestController,
                "jobStreamEmitters"))).add(emitterMock);

        when(jobManager.getNumSlots()).thenReturn(1);
        when(jobManager.countJobExecutions(JobExecutionState.RUNNING)).thenReturn(2);
        when(jobManager.countJobExecutions(JobExecutionState.WAITING)).thenReturn(3);
        when(jobManager.countJobExecutions(JobExecutionState.FAILED)).thenReturn(4);

        jobRestController.onJobEvent(
                new JobEvent(JobEventType.STATE_CHANGE,
                        Job.builder().id("job-id").name("job-name").currentJobExecution(jobExecution).build())
        );

        ArgumentCaptor<SseEmitter.SseEventBuilder> argCap = ArgumentCaptor.forClass(SseEmitter.SseEventBuilder.class);
        verify(emitterMock, times(2)).send(argCap.capture());

        // First Event
        Iterator<ResponseBodyEmitter.DataWithMediaType> eventPartsIterator = argCap.getAllValues().getFirst().build().iterator();

        ResponseBodyEmitter.DataWithMediaType eventPart = eventPartsIterator.next();
        assertEquals("event:state-update\ndata:", eventPart.getData());

        eventPart = eventPartsIterator.next();
        JobListEntry jobListEntry = (JobListEntry) eventPart.getData();
        assertEquals("job-id", jobListEntry.getId());
        assertEquals("job-name", jobListEntry.getName());
        assertEquals(42L, jobListEntry.getExecution().getId());
        assertEquals(JobExecutionState.FINISHED.name(), jobListEntry.getExecution().getState());

        // Second Event
        eventPartsIterator = argCap.getAllValues().get(1).build().iterator();

        eventPart = eventPartsIterator.next();
        assertEquals("event:execution-overview\ndata:", eventPart.getData());

        eventPart = eventPartsIterator.next();
        JobExecutionOverview jobExecutionOverview = (JobExecutionOverview) eventPart.getData();
        assertEquals(1, jobExecutionOverview.getNumSlots());
        assertEquals(2, jobExecutionOverview.getNumRunning());
        assertEquals(3, jobExecutionOverview.getNumWaiting());
        assertEquals(4, jobExecutionOverview.getNumFailed());
    }

    /**
     * Tests handling job CRUD events.
     */
    @Test
    @DisplayName("Tests handling job CRUD events.")
    @SneakyThrows
    void testOnJobEventCrud() {
        SseEmitter emitterMock = mock(SseEmitter.class);

        //noinspection unchecked
        ((CopyOnWriteArrayList<SseEmitter>) Objects.requireNonNull(ReflectionTestUtils.getField(jobRestController,
                "jobStreamEmitters"))).add(emitterMock);

        when(jobManager.getNumSlots()).thenReturn(1);
        when(jobManager.countJobExecutions(JobExecutionState.RUNNING)).thenReturn(2);
        when(jobManager.countJobExecutions(JobExecutionState.WAITING)).thenReturn(3);
        when(jobManager.countJobExecutions(JobExecutionState.FAILED)).thenReturn(4);

        jobRestController.onJobEvent(
                new JobEvent(JobEventType.DELETE, Job.builder().id("job-id").name("job-name").build())
        );

        ArgumentCaptor<SseEmitter.SseEventBuilder> argCap = ArgumentCaptor.forClass(SseEmitter.SseEventBuilder.class);
        verify(emitterMock, times(2)).send(argCap.capture());

        // First Event
        Iterator<ResponseBodyEmitter.DataWithMediaType> eventPartsIterator = argCap.getAllValues().getFirst().build().iterator();

        ResponseBodyEmitter.DataWithMediaType eventPart = eventPartsIterator.next();
        assertEquals("event:crud\ndata:", eventPart.getData());

        eventPart = eventPartsIterator.next();
        assertEquals("job-id", eventPart.getData());

        // Second Event
        eventPartsIterator = argCap.getAllValues().get(1).build().iterator();

        eventPart = eventPartsIterator.next();
        assertEquals("event:execution-overview\ndata:", eventPart.getData());

        eventPart = eventPartsIterator.next();
        JobExecutionOverview jobExecutionOverview = (JobExecutionOverview) eventPart.getData();
        assertEquals(1, jobExecutionOverview.getNumSlots());
        assertEquals(2, jobExecutionOverview.getNumRunning());
        assertEquals(3, jobExecutionOverview.getNumWaiting());
        assertEquals(4, jobExecutionOverview.getNumFailed());
    }

    /**
     * Tests cleaning up dead emitters on job events.
     */
    @Test
    @DisplayName("Tests cleaning up dead emitters on job events.")
    @SneakyThrows
    void testOnJobEventCleanup() {
        SseEmitter emitterMock = mock(SseEmitter.class);

        JobExecution jobExecution = JobExecution.builder().build();

        //noinspection unchecked
        CopyOnWriteArrayList<SseEmitter> emitters =
                (CopyOnWriteArrayList<SseEmitter>) Objects.requireNonNull(ReflectionTestUtils.getField(jobRestController,
                        "jobStreamEmitters"));
        emitters.add(emitterMock);

        doThrow(new IOException("wanted-test-exception")).when(emitterMock).send(any(SseEmitter.SseEventBuilder.class));

        jobRestController.onJobEvent(
                new JobEvent(JobEventType.STATE_REFRESH,
                        Job.builder().currentJobExecution(jobExecution).build())
        );

        // A dead emitter must be removed if sending fails:
        assertFalse(emitters.contains(emitterMock));
    }

}

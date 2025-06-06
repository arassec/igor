package com.arassec.igor.web.controller;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.event.JobEvent;
import com.arassec.igor.core.util.event.JobEventType;
import com.arassec.igor.web.model.JobExecutionListEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the {@link ExecutionRestController}.
 */
@DisplayName("Execution-Controller Tests")
class ExecutionRestControllerTest extends RestControllerBaseTest {

    /**
     * The REST controller for job executions.
     */
    @Autowired
    private ExecutionRestController executionRestController;

    /**
     * Tests getting a stream of execution data.
     */
    @Test
    @DisplayName("Tests getting a stream of execution data.")
    @SneakyThrows
    void testGetExecutionStream() {
        MvcResult mvcResult = mockMvc.perform(get("/api/execution/stream")).andExpect(status().isOk()).andReturn();

        // Simulate an event:
        executionRestController.onJobEvent(new JobEvent(JobEventType.STATE_CHANGE,
                Job.builder().id("job-id").name("job-name").currentJobExecution(
                        JobExecution.builder().executionState(JobExecutionState.RUNNING).build()
                ).build()));

        String streamContent = mvcResult.getResponse().getContentAsString();
        assertEquals("""
            data:{"executionState":"RUNNING","processedEvents":0,"workInProgress":[],\
            "runningOrActive":true}

            """, streamContent);
    }

    /**
     * Tests getting executions of a job with an empty page as result.
     */
    @Test
    @DisplayName("Tests getting executions of a job with an empty page as result.")
    @SneakyThrows
    void testGetExecutionsOfJobEmptyPage() {
        MvcResult mvcResult = mockMvc.perform(get("/api/execution/job/job-id")).andExpect(status().isOk()).andReturn();
        ModelPage<JobExecutionListEntry> result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(0, result.getNumber());
        assertEquals(Integer.MAX_VALUE, result.getSize());
        assertEquals(0, result.getTotalPages());
        assertNotNull(result.getItems());
    }

    /**
     * Tests getting executions of a job.
     */
    @Test
    @DisplayName("Tests getting executions of a job.")
    @SneakyThrows
    void testGetExecutionsOfJob() {
        ModelPage<JobExecution> jobExecutions = new ModelPage<>(1, 2, 3,
                List.of(JobExecution.builder().id(123L).jobId("job-id")
                                .executionState(JobExecutionState.FAILED).created(Instant.now()).build(),
                        JobExecution.builder().id(456L).jobId("job-id")
                                .executionState(JobExecutionState.RUNNING).created(Instant.now()).build(),
                        JobExecution.builder().id(789L).jobId("job-id")
                                .executionState(JobExecutionState.WAITING).created(Instant.now()).build()));

        when(jobManager.getJobExecutionsOfJob("job-id", 666, 42)).thenReturn(jobExecutions);

        when(jobManager.load("job-id")).thenReturn(Job.builder().name("job-name").build());

        MvcResult mvcResult = mockMvc.perform(get("/api/execution/job/job-id")
                .param("pageNumber", "666")
                .param("pageSize", "42")).andExpect(status().isOk()).andReturn();
        ModelPage<JobExecutionListEntry> result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(666, result.getNumber());
        assertEquals(42, result.getSize());
        assertEquals(3, result.getTotalPages());
        assertEquals(3, result.getItems().size());

        JobExecutionListEntry jobExecutionListEntry = result.getItems().getFirst();
        assertEquals(789L, jobExecutionListEntry.getId());
        assertEquals("job-id", jobExecutionListEntry.getJobId());
        assertEquals("job-name", jobExecutionListEntry.getJobName());
        assertEquals(JobExecutionState.WAITING.name(), jobExecutionListEntry.getState());
        assertNotNull(jobExecutionListEntry.getCreated());
        assertNotNull(jobExecutionListEntry.getDuration());

        jobExecutionListEntry = result.getItems().get(1);
        assertEquals(456L, jobExecutionListEntry.getId());
        assertEquals("job-id", jobExecutionListEntry.getJobId());
        assertEquals("job-name", jobExecutionListEntry.getJobName());
        assertEquals(JobExecutionState.RUNNING.name(), jobExecutionListEntry.getState());
        assertNotNull(jobExecutionListEntry.getCreated());
        assertNotNull(jobExecutionListEntry.getDuration());

        jobExecutionListEntry = result.getItems().get(2);
        assertEquals(123L, jobExecutionListEntry.getId());
        assertEquals("job-id", jobExecutionListEntry.getJobId());
        assertEquals("job-name", jobExecutionListEntry.getJobName());
        assertEquals(JobExecutionState.FAILED.name(), jobExecutionListEntry.getState());
        assertNotNull(jobExecutionListEntry.getCreated());
        assertEquals("", jobExecutionListEntry.getDuration());
    }

    /**
     * Tests counting executions in a certain state of a job.
     */
    @Test
    @DisplayName("Tests counting executions in a certain state of a job.")
    @SneakyThrows
    void testCountExecutionsOfJobInState() {
        mockMvc.perform(get("/api/execution/job/job-id/WAITING/count")).andExpect(status().isOk())
                .andExpect(content().string("0"));

        ModelPage<JobExecution> jobExecutions = new ModelPage<>(1, 2, 3,
                List.of(JobExecution.builder().id(123L).jobId("job-id")
                                .executionState(JobExecutionState.FAILED).created(Instant.now()).build(),
                        JobExecution.builder().id(456L).jobId("job-id")
                                .executionState(JobExecutionState.FAILED).created(Instant.now()).build(),
                        JobExecution.builder().id(789L).jobId("job-id")
                                .executionState(JobExecutionState.WAITING).created(Instant.now()).build()));

        when(jobManager.getJobExecutionsOfJob("job-id", 0, Integer.MAX_VALUE)).thenReturn(jobExecutions);

        mockMvc.perform(get("/api/execution/job/job-id/FAILED/count")).andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    /**
     * Tests getting execution details.
     */
    @Test
    @DisplayName("Tests getting execution details.")
    @SneakyThrows
    void testGetDetailedExecution() {
        mockMvc.perform(get("/api/execution/details/123")).andExpect(status().isBadRequest());

        when(jobManager.getJobExecution(123L)).thenReturn(JobExecution.builder().id(456L).build());

        MvcResult mvcResult = mockMvc.perform(get("/api/execution/details/123")).andExpect(status().isOk()).andReturn();

        JobExecution result = convert(mvcResult, JobExecution.class);
        assertEquals(456L, result.getId());
    }

    /**
     * Tests cancelling a certain execution.
     */
    @Test
    @DisplayName("Tests cancelling a certain execution.")
    @SneakyThrows
    void testCancelExecution() {
        mockMvc.perform(post("/api/execution/-123/cancel")).andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/execution/123/cancel")).andExpect(status().isNoContent());
        verify(jobManager, times(1)).cancelExecution(123L);
    }

    /**
     * Tests updating an execution's state.
     */
    @Test
    @DisplayName("Tests updating an execution's state.")
    @SneakyThrows
    void testUpdateExecutionState() {
        mockMvc.perform(
                put("/api/execution/123/job-id/" + JobExecutionState.FAILED.name() + "/" + JobExecutionState.CANCELLED.name()))
                .andExpect(status().isNoContent());
        verify(jobManager, times(1)).updateJobExecutionState(123L, JobExecutionState.CANCELLED);

        mockMvc.perform(
                put("/api/execution/123/job-id/" + JobExecutionState.FAILED.name() + "/" + JobExecutionState.RESOLVED.name())
                        .param("updateAllOfJob", "true"))
                .andExpect(status().isNoContent());
        verify(jobManager, times(1)).updateAllJobExecutionsOfJob("job-id", JobExecutionState.FAILED, JobExecutionState.RESOLVED);
    }

    /**
     * Tests handling job events.
     */
    @Test
    @DisplayName("Tests handling job events.")
    @SneakyThrows
    void testOnJobEvent() {
        SseEmitter emitterMock = mock(SseEmitter.class);

        JobExecution jobExecution = JobExecution.builder().build();

        //noinspection unchecked
        ((CopyOnWriteArrayList<SseEmitter>) Objects.requireNonNull(ReflectionTestUtils.getField(executionRestController, "executionStreamEmitters")))
                .add(emitterMock);

        executionRestController.onJobEvent(
                new JobEvent(JobEventType.STATE_REFRESH,
                        Job.builder().currentJobExecution(jobExecution).build())
        );

        verify(emitterMock, times(1)).send(jobExecution);
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
        CopyOnWriteArrayList<SseEmitter> emitters = (CopyOnWriteArrayList<SseEmitter>) Objects.requireNonNull(ReflectionTestUtils.getField(executionRestController, "executionStreamEmitters"));
        emitters.add(emitterMock);

        doThrow(new IOException("wanted-test-exception")).when(emitterMock).send(any(JobExecution.class));

        executionRestController.onJobEvent(
                new JobEvent(JobEventType.STATE_REFRESH,
                        Job.builder().currentJobExecution(jobExecution).build())
        );

        // A dead emitter must be removed if sending fails:
        assertFalse(emitters.contains(emitterMock));
    }

}

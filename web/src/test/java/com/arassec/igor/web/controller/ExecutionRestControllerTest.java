package com.arassec.igor.web.controller;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.web.model.JobExecutionListEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests the {@link ExecutionRestController}.
 */
@DisplayName("Execution-Controller Tests")
class ExecutionRestControllerTest extends RestControllerBaseTest {

    /**
     * Tests getting an execution overview.
     */
    @Test
    @DisplayName("Tests getting an execution overview.")
    @SneakyThrows
    void testGetJobExecutionOverview() {
        when(jobManager.getNumSlots()).thenReturn(1);
        when(jobManager.countJobExecutions(eq(JobExecutionState.RUNNING))).thenReturn(2);
        when(jobManager.countJobExecutions(eq(JobExecutionState.WAITING))).thenReturn(3);
        when(jobManager.countJobExecutions(eq(JobExecutionState.FAILED))).thenReturn(4);

        mockMvc.perform(get("/api/execution/overview")).andExpect(status().isOk())
                .andExpect(jsonPath("$.numSlots").value("1"))
                .andExpect(jsonPath("$.numRunning").value("2"))
                .andExpect(jsonPath("$.numWaiting").value("3"))
                .andExpect(jsonPath("$.numFailed").value("4"));
    }


    /**
     * Tests getting executions of a job.
     */
    @Test
    @DisplayName("Tests getting executions of a job.")
    @SneakyThrows
    void testGetExecutionsOfJob() {
        // Test empty page:
        MvcResult mvcResult = mockMvc.perform(get("/api/execution/job/job-id")).andExpect(status().isOk()).andReturn();
        ModelPage<JobExecutionListEntry> result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(0, result.getNumber());
        assertEquals(Integer.MAX_VALUE, result.getSize());
        assertEquals(0, result.getTotalPages());
        assertNotNull(result.getItems());

        // With actual job executions in the result:
        ModelPage<JobExecution> jobExecutions = new ModelPage<>(1, 2, 3,
                List.of(JobExecution.builder().id(123L).jobId("job-id")
                                .executionState(JobExecutionState.FAILED).created(Instant.now()).build(),
                        JobExecution.builder().id(456L).jobId("job-id")
                                .executionState(JobExecutionState.RUNNING).created(Instant.now()).build(),
                        JobExecution.builder().id(789L).jobId("job-id")
                                .executionState(JobExecutionState.WAITING).created(Instant.now()).build()));

        when(jobManager.getJobExecutionsOfJob(eq("job-id"), eq(666), eq(42))).thenReturn(jobExecutions);

        when(jobManager.load(eq("job-id"))).thenReturn(Job.builder().name("job-name").build());

        mvcResult = mockMvc.perform(get("/api/execution/job/job-id")
                .param("pageNumber", "666")
                .param("pageSize", "42")).andExpect(status().isOk()).andReturn();
        result = convert(mvcResult, new TypeReference<>() {
        });

        assertEquals(666, result.getNumber());
        assertEquals(42, result.getSize());
        assertEquals(3, result.getTotalPages());
        assertEquals(3, result.getItems().size());

        JobExecutionListEntry jobExecutionListEntry = result.getItems().get(0);
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

        when(jobManager.getJobExecutionsOfJob(eq("job-id"), eq(0), eq(Integer.MAX_VALUE))).thenReturn(jobExecutions);

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

        when(jobManager.getJobExecution(eq(123L))).thenReturn(JobExecution.builder().id(456L).build());

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
        verify(jobManager, times(1)).cancelExecution(eq(123L));
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
        verify(jobManager, times(1)).updateJobExecutionState(eq(123L), eq(JobExecutionState.CANCELLED));

        mockMvc.perform(
                put("/api/execution/123/job-id/" + JobExecutionState.FAILED.name() + "/" + JobExecutionState.RESOLVED.name())
                        .param("updateAllOfJob", "true"))
                .andExpect(status().isNoContent());
        verify(jobManager, times(1)).updateAllJobExecutionsOfJob(eq("job-id"), eq(JobExecutionState.FAILED), eq(JobExecutionState.RESOLVED));
    }

}
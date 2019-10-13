package com.arassec.igor.web.controller;

import com.arassec.igor.core.application.JobManager;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.web.model.JobExecutionListEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST-Controller for {@link JobExecution}s.
 */
@RestController
@RequestMapping("/api/execution")
@RequiredArgsConstructor
public class ExecutionRestController {

    /**
     * Manager for Jobs.
     */
    private final JobManager jobManager;

    /**
     * Returns the execution states of a certain job.
     *
     * @param jobId      The job's ID.
     * @param pageNumber The number of the page to load.
     * @param pageSize   The number of elements in one page.
     * @return The saved {@link JobExecution}s with information about their state or {@code null}, if the job has never
     * been executed.
     */
    @GetMapping("job/{jobId}")
    public ModelPage<JobExecutionListEntry> getExecutionsOfJob(@PathVariable("jobId") Long jobId,
                                                               @RequestParam(value = "pageNumber", required = false,
                                                                       defaultValue = "0") int pageNumber,
                                                               @RequestParam(value = "pageSize", required = false,
                                                                       defaultValue = "2147483647") int pageSize) {
        ModelPage<JobExecution> jobExecutions = jobManager.getJobExecutionsOfJob(jobId, pageNumber, pageSize);

        if (jobExecutions != null) {
            Job job = jobManager.load(jobId);

            ModelPage<JobExecutionListEntry> result = new ModelPage<>(pageNumber, pageSize, jobExecutions.getTotalPages(), null);
            result.setItems(jobExecutions.getItems().stream().map(jobExecution -> convert(jobExecution, job.getName()))
                    .collect(Collectors.toList()));
            result.getItems().sort(Comparator.comparing(JobExecutionListEntry::getId).reversed());

            return result;
        }

        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
    }

    /**
     * Returns the number of executions of a certain job which are in the supplied state.
     *
     * @param jobId The job's ID.
     * @param state The execution's state.
     * @return The saved {@link JobExecution}s with information about their state or {@code null}, if the job has never
     * been executed.
     */
    @GetMapping("job/{jobId}/{state}/count")
    public Long countExecutionsOfJobInState(@PathVariable("jobId") Long jobId, @PathVariable("state") JobExecutionState state) {
        ModelPage<JobExecution> jobExecutions = jobManager.getJobExecutionsOfJob(jobId, 0, Integer.MAX_VALUE);
        if (jobExecutions != null && jobExecutions.getItems() != null) {
            return jobExecutions.getItems().stream().filter(jobExecution -> jobExecution.getExecutionState().equals(state))
                    .count();
        }
        return 0L;
    }

    /**
     * Returns the job IDs of those jobs, which are in one of the requested states (e.g. RUNNING or WAITING).
     *
     * @param states The states to get job IDs for.
     * @return List of Job IDs.
     */
    @GetMapping("jobs")
    public List<Long> getJobIdsInState(@RequestParam("states") Set<JobExecutionState> states) {
        List<JobExecution> executions = new LinkedList<>();
        if (states != null && !states.isEmpty()) {
            states.forEach(state -> {
                ModelPage<JobExecution> jobExecutionsInState = jobManager.getJobExecutionsInState(state, 0, Integer.MAX_VALUE);
                if (jobExecutionsInState != null && jobExecutionsInState.getItems() != null) {
                    executions.addAll(jobExecutionsInState.getItems());
                }
            });
        }
        return executions.stream().map(jobExecution -> jobExecution.getJobId()).collect(Collectors.toList());
    }

    /**
     * Returns the execution details with the given ID.
     *
     * @param id The execution details' ID.
     * @return The details.
     */
    @GetMapping("details/{id}")
    public JobExecution getDetailedExecution(@PathVariable("id") Long id) {
        JobExecution jobExecution = jobManager.getJobExecution(id);
        if (jobExecution != null) {
            return jobExecution;
        }
        throw new IllegalArgumentException("No job-execution with the requested ID available.");
    }

    /**
     * Returns the number of available slots for parallel job execution.
     *
     * @return The number of slots.
     */
    @GetMapping("numSlots")
    public int getNumSlots() {
        return jobManager.getNumSlots();
    }

    /**
     * Returns the execution states which are in the specified state.
     *
     * @param state      The desired state of the job executions.
     * @param pageNumber The number of the page to load.
     * @param pageSize   The number of elements in one page.
     * @return A list with all executions in the specified state.
     */
    @GetMapping(value = "{state}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelPage<JobExecutionListEntry> getExecutionsByState(@PathVariable("state") JobExecutionState state,
                                                                 @RequestParam(value = "pageNumber", required = false,
                                                                         defaultValue = "0") int pageNumber,
                                                                 @RequestParam(value = "pageSize", required = false,
                                                                         defaultValue = "2147483647") int pageSize) {
        ModelPage<JobExecution> jobExecutions = jobManager.getJobExecutionsInState(state, pageNumber, pageSize);

        if (jobExecutions != null && jobExecutions.getItems() != null && !jobExecutions.getItems().isEmpty()) {
            ModelPage<JobExecutionListEntry> result = new ModelPage<>(pageNumber, pageSize, jobExecutions.getTotalPages(), null);

            result.setItems(jobExecutions.getItems().stream().map(jobExecution -> {
                Job job = jobManager.load(jobExecution.getJobId());
                return convert(jobExecution, job.getName());
            }).collect(Collectors.toList()));

            return result;
        }

        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
    }


    /**
     * Cancels a running or waiting job execution.
     *
     * @param id The job-execution's ID.
     */
    @PostMapping("{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelJob(@PathVariable("id") Long id) {
        if (id == null || id < 0) {
            throw new IllegalArgumentException("Invalid ID");
        }
        jobManager.cancel(id);
    }

    /**
     * Updates the state of all job executions, either identified by a single execution ID, or by the job's ID.
     *
     * @param id             The ID of a single execution.
     * @param jobId          The job's ID. Relevant only if all executions of the job should be updated.
     * @param oldState       The old state. Only relevant if all executions of the job should be updated.
     * @param newState       The new state to set to the execution.
     * @param updateAllOfJob Set to {@code true}, if all executions of the supplied job should be updated.
     */
    @PutMapping("{id}/{jobId}/{oldState}/{newState}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateExecutionState(@PathVariable("id") Long id, @PathVariable("jobId") Long jobId,
                                     @PathVariable("oldState") JobExecutionState oldState,
                                     @PathVariable("newState") JobExecutionState newState,
                                     @RequestParam(value = "updateAllOfJob", required = false, defaultValue = "false") boolean updateAllOfJob) {
        if (updateAllOfJob) {
            jobManager.updateAllJobExecutionsOfJob(jobId, oldState, newState);
        } else {
            jobManager.updateJobExecutionState(id, newState);
        }

    }

    /**
     * Converts a job-execution into a list entry for the UI.
     *
     * @param jobExecution The job-execution.
     * @return The {@link JobExecutionListEntry} for the UI.
     */
    private JobExecutionListEntry convert(JobExecution jobExecution, String jobName) {
        JobExecutionListEntry listEntry = new JobExecutionListEntry();
        listEntry.setId(jobExecution.getId());
        listEntry.setJobId(jobExecution.getJobId());
        listEntry.setJobName(jobName);
        listEntry.setState(jobExecution.getExecutionState().name());
        listEntry.setCreated(jobExecution.getCreated());
        if (JobExecutionState.RUNNING.equals(jobExecution.getExecutionState()) && (jobExecution.getStarted() != null)) {
            listEntry.setDuration(formatDuration(Duration.between(jobExecution.getStarted(), Instant.now()).toSeconds()));
        } else if (JobExecutionState.WAITING.equals(jobExecution.getExecutionState())) {
            listEntry.setDuration(formatDuration(Duration.between(jobExecution.getCreated(), Instant.now()).toSeconds()));
        } else if (jobExecution.getStarted() != null && jobExecution.getFinished() != null) {
            listEntry.setDuration(
                    formatDuration(Duration.between(jobExecution.getStarted(), jobExecution.getFinished()).toSeconds()));
        } else {
            listEntry.setDuration("");
        }
        return listEntry;
    }

    /**
     * Formats the supplied time in milliseconds as HH:MM:SS.
     *
     * @param timeInMillis The time in milliseconds.
     * @return The formatted time.
     */
    private String formatDuration(Long timeInMillis) {
        return String.format("(%02d:%02d:%02d)", timeInMillis / 3600, (timeInMillis % 3600) / 60, (timeInMillis % 60));
    }

}

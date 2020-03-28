package com.arassec.igor.web.controller;

import com.arassec.igor.core.application.JobManager;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.web.model.JobExecutionListEntry;
import com.arassec.igor.web.model.JobExecutionOverview;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST-Controller for {@link JobExecution}s.
 */
@RestController
@RequestMapping("/api/execution")
@RequiredArgsConstructor
public class ExecutionRestController extends BaseRestController {

    /**
     * Manager for Jobs.
     */
    private final JobManager jobManager;

    /**
     * Returns an overview of the job executions.
     *
     * @return A {@link JobExecutionOverview}.
     */
    @GetMapping("overview")
    public JobExecutionOverview getJobExecutionOverview() {
        JobExecutionOverview result = new JobExecutionOverview();
        result.setNumSlots(jobManager.getNumSlots());
        result.setNumRunning(jobManager.countJobExecutions(JobExecutionState.RUNNING));
        result.setNumWaiting(jobManager.countJobExecutions(JobExecutionState.WAITING));
        result.setNumFailed(jobManager.countJobExecutions(JobExecutionState.FAILED));
        return result;
    }

    /**
     * Returns the execution states of a certain job.
     *
     * @param jobId      The job's ID.
     * @param pageNumber The number of the page to load.
     * @param pageSize   The number of elements in one page.
     *
     * @return The saved {@link JobExecution}s with information about their state or {@code null}, if the job has never been
     * executed.
     */
    @GetMapping("job/{jobId}")
    public ModelPage<JobExecutionListEntry> getExecutionsOfJob(@PathVariable("jobId") String jobId,
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
     *
     * @return The saved {@link JobExecution}s with information about their state or {@code null}, if the job has never been
     * executed.
     */
    @GetMapping("job/{jobId}/{state}/count")
    public Long countExecutionsOfJobInState(@PathVariable("jobId") String jobId, @PathVariable("state") JobExecutionState state) {
        ModelPage<JobExecution> jobExecutions = jobManager.getJobExecutionsOfJob(jobId, 0, Integer.MAX_VALUE);
        if (jobExecutions != null && jobExecutions.getItems() != null) {
            return jobExecutions.getItems().stream().filter(jobExecution -> jobExecution.getExecutionState().equals(state))
                    .count();
        }
        return 0L;
    }

    /**
     * Returns the execution details with the given ID.
     *
     * @param id The execution details' ID.
     *
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
     * Cancels a running or waiting job execution.
     *
     * @param id The job-execution's ID.
     */
    @PostMapping("{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelExecution(@PathVariable("id") Long id) {
        if (id == null || id < 0) {
            throw new IllegalArgumentException("Invalid ID");
        }
        jobManager.cancelExecution(id);
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
    public void updateExecutionState(@PathVariable("id") Long id, @PathVariable("jobId") String jobId,
                                     @PathVariable("oldState") JobExecutionState oldState,
                                     @PathVariable("newState") JobExecutionState newState,
                                     @RequestParam(value = "updateAllOfJob", required = false, defaultValue = "false") boolean updateAllOfJob) {
        if (updateAllOfJob) {
            jobManager.updateAllJobExecutionsOfJob(jobId, oldState, newState);
        } else {
            jobManager.updateJobExecutionState(id, newState);
        }

    }

}

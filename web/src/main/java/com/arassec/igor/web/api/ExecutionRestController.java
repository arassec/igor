package com.arassec.igor.web.api;

import com.arassec.igor.core.application.JobManager;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.web.api.model.JobExecutionListEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST-Controller for {@link JobExecution}s.
 */
@RestController
@RequestMapping("/api/execution")
public class ExecutionRestController {

    /**
     * Manager for Jobs.
     */
    @Autowired
    private JobManager jobManager;

    /**
     * Returns the execution states of a certain job.
     *
     * @param jobId The job's ID.
     * @return The saved {@link JobExecution}s with information about their state or {@code null}, if the job has never
     * been executed.
     */
    @GetMapping("job/{jobId}")
    public List<JobExecutionListEntry> getExecutionsOfJob(@PathVariable("jobId") Long jobId) {
        List<JobExecutionListEntry> result = List.of();
        Job job = jobManager.load(jobId);
        List<JobExecution> jobExecutions = jobManager.getJobExecutionsOfJob(jobId);
        if (jobExecutions != null) {
            result = jobExecutions.stream().map(jobExecution -> convert(jobExecution, job.getName())).collect(Collectors.toList());
            result.sort(Comparator.comparing(JobExecutionListEntry::getId).reversed());
        }
        return result;
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
     * @param state The desired state of the job executions.
     * @return A list with all executions in the specified state.
     */
    @GetMapping(value = "{state}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<JobExecutionListEntry> getExecutionsByState(@PathVariable("state") JobExecutionState state) {
        List<JobExecutionListEntry> result = List.of();
        List<JobExecution> jobExecutions = jobManager.getJobExecutionsInState(state);
        if (jobExecutions != null) {
            result = jobExecutions.stream().map(jobExecution -> {
                Job job = jobManager.load(jobExecution.getJobId());
                return convert(jobExecution, job.getName());
            }).collect(Collectors.toList());
            result.sort(Comparator.comparing(JobExecutionListEntry::getDuration).reversed());
        }
        return result;
    }


    /**
     * Cancels a running or waiting job execution.
     *
     * @param id The job-execution's ID.
     * @return 'OK' on success.
     */
    @PostMapping("{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelJob(@PathVariable("id") Long id) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("ID required");
        }
        jobManager.cancel(id);
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
        } else if (jobExecution.getStarted() != null && jobExecution.getFinished() != null){
            listEntry.setDuration(formatDuration(Duration.between(jobExecution.getStarted(), jobExecution.getFinished()).toSeconds()));
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

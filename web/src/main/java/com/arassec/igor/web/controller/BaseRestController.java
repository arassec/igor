package com.arassec.igor.web.controller;

import com.arassec.igor.application.manager.JobManager;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.web.model.JobExecutionListEntry;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;

/**
 * Base class for REST-Controllers.
 */
@Slf4j
public abstract class BaseRestController {

    /**
     * Converts a job-execution into a list entry for the UI.
     *
     * @param jobExecution The job-execution.
     * @param jobName      The job's name.
     *
     * @return The {@link JobExecutionListEntry} for the UI.
     */
    protected JobExecutionListEntry convert(JobExecution jobExecution, String jobName) {
        if (jobExecution == null) {
            return null;
        }
        JobExecutionListEntry listEntry = new JobExecutionListEntry();
        listEntry.setId(jobExecution.getId());
        listEntry.setJobId(jobExecution.getJobId());
        listEntry.setJobName(jobName);
        if (jobExecution.getExecutionState() != null) {
            listEntry.setState(jobExecution.getExecutionState().name());
        }
        listEntry.setCreated(jobExecution.getCreated());
        listEntry.setStarted(jobExecution.getStarted());
        listEntry.setFinished(jobExecution.getFinished());
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
     * Determines the most recent {@link JobExecution} of the given job.
     *
     * @param jobManager The manager for jobs.
     * @param job        The {@link Job} to get the execution for.
     *
     * @return The most recent {@link JobExecution} if available, {@code null} otherwise.
     */
    protected JobExecution determineJobExecution(JobManager jobManager, Job job) {
        JobExecution jobExecution = job.getCurrentJobExecution();
        if (jobExecution == null) {
            ModelPage<JobExecution> jobExecutionsOfJob = jobManager.getJobExecutionsOfJob(job.getId(), 0, 1);
            if (jobExecutionsOfJob != null && !jobExecutionsOfJob.getItems().isEmpty()) {
                jobExecution = jobExecutionsOfJob.getItems().get(0);
            }
        }
        return jobExecution;
    }

    /**
     * Formats the supplied time in milliseconds as HH:MM:SS.
     *
     * @param timeInMillis The time in milliseconds.
     *
     * @return The formatted time.
     */
    private String formatDuration(Long timeInMillis) {
        return String.format("(%02d:%02d:%02d)", timeInMillis / 3600, (timeInMillis % 3600) / 60, (timeInMillis % 60));
    }

}

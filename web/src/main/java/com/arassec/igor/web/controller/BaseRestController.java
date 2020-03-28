package com.arassec.igor.web.controller;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.web.model.JobExecutionListEntry;
import com.arassec.igor.web.model.KeyLabelStore;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Base class for REST-Controllers.
 */
public abstract class BaseRestController {

    /**
     * Sorts the supplied {@link KeyLabelStore} items by their label.
     *
     * @param input The {@link KeyLabelStore}s to sort.
     * @return The sorted list.
     */
    protected List<KeyLabelStore> sortByLabel(Set<KeyLabelStore> input) {
        List<KeyLabelStore> result = new LinkedList<>(input);
        result.sort(Comparator.comparing(KeyLabelStore::getValue));
        return result;
    }

    /**
     * Converts a job-execution into a list entry for the UI.
     *
     * @param jobExecution The job-execution.
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
        listEntry.setState(jobExecution.getExecutionState().name());
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

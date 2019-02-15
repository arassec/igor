package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.github.openjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * JSON-Converter for {@link JobExecution}s.
 */
@Slf4j
@Component
public class JsonJobExecutionConverter {

    /**
     * Converts a {@link JobExecution} into its JSON representation.
     *
     * @param jobExecution The job-execution to convert.
     * @return The JSON.
     */
    public JSONObject convert(JobExecution jobExecution) {
        if (jobExecution == null) {
            return null;
        }
        JSONObject result = new JSONObject();
        if (jobExecution.getId() != null) {
            result.put(JsonKeys.ID, jobExecution.getId());
        }
        result.put(JsonKeys.JOB_ID, jobExecution.getJobId());
        result.put(JsonKeys.CREATED, jobExecution.getCreated().toEpochMilli());
        result.put(JsonKeys.STATE, jobExecution.getExecutionState().name());
        if (jobExecution.getStarted() != null) {
            result.put(JsonKeys.STARTED, jobExecution.getStarted().toEpochMilli());
        }
        if (jobExecution.getFinished() != null) {
            result.put(JsonKeys.FINISHED, jobExecution.getFinished().toEpochMilli());
        }
        if (jobExecution.getErrorCause() != null) {
            result.put(JsonKeys.ERROR_CAUSE, jobExecution.getErrorCause());
        }
        if (jobExecution.getCurrentTask() != null) {
            result.put(JsonKeys.CURRENT_TASK, jobExecution.getCurrentTask());
        }
        return result;
    }

    /**
     * Converts a job-execution JSON into an object instance.
     *
     * @param jobExecutionJson The job-exeuction in JSON form.
     * @return A {@link JobExecution} instance.
     */
    public JobExecution convert(JSONObject jobExecutionJson) {
        if (jobExecutionJson == null) {
            return null;
        }
        JobExecution result = new JobExecution();
        long id = jobExecutionJson.optLong(JsonKeys.ID, -1);
        if (id >= 0) {
            result.setId(id);
        }
        result.setJobId(jobExecutionJson.getLong(JsonKeys.JOB_ID));
        result.setCreated(Instant.ofEpochMilli(jobExecutionJson.getLong(JsonKeys.CREATED)));
        long started = jobExecutionJson.optLong(JsonKeys.STARTED, -1);
        if (started > 0) {
            result.setStarted(Instant.ofEpochMilli(jobExecutionJson.getLong(JsonKeys.STARTED)));
        }
        long finished = jobExecutionJson.optLong(JsonKeys.FINISHED, -1);
        if (finished > 0) {
            result.setFinished(Instant.ofEpochMilli(jobExecutionJson.getLong(JsonKeys.FINISHED)));
        }
        result.setExecutionState(JobExecutionState.valueOf(jobExecutionJson.getString(JsonKeys.STATE)));
        result.setErrorCause(jobExecutionJson.optString(JsonKeys.ERROR_CAUSE));
        result.setCurrentTask(jobExecutionJson.optString(JsonKeys.CURRENT_TASK));
        return result;
    }

}

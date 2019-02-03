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
        result.put(JsonKeys.STARTED, jobExecution.getStarted().toEpochMilli());
        result.put(JsonKeys.FINISHED, jobExecution.getFinished().toEpochMilli());
        result.put(JsonKeys.STATE, jobExecution.getExecutionState().name());
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
        result.setStarted(Instant.ofEpochMilli(jobExecutionJson.getLong(JsonKeys.STARTED)));
        result.setFinished(Instant.ofEpochMilli(jobExecutionJson.getLong(JsonKeys.FINISHED)));
        result.setExecutionState(JobExecutionState.valueOf(jobExecutionJson.getString(JsonKeys.STATE)));
        result.setErrorCause(jobExecutionJson.optString(JsonKeys.ERROR_CAUSE));
        result.setCurrentTask(jobExecutionJson.optString(JsonKeys.CURRENT_TASK));
        return result;
    }

}

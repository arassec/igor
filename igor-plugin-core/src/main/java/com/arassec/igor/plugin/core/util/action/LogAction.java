package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreCategory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samskivert.mustache.Mustache;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h2>Log Action</h2>
 *
 * <h3>Description</h3>
 * This action logs every processed data item to igor's log with DEBUG level.
 */
@Slf4j
@Getter
@Setter
@IgorComponent(typeId = "log-action", categoryId = CoreCategory.UTIL)
public class LogAction extends BaseAction {

    /**
     * The job's ID.
     */
    private String jobId;

    /**
     * The loglevel to use for logging the message. Must be one of 'error', 'warn', 'info', 'debug' or 'trace'.
     */
    @NotBlank
    @Pattern(regexp = "error|warn|info|debug|trace")
    @IgorParam
    private String level = "debug";

    /**
     * The message to log. Supports mustache templates to log parts of or the complete data item being processed.
     */
    @NotBlank
    @IgorParam
    private String message = "{{.}}";

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(JobExecution jobExecution) {
        super.initialize(jobExecution);
        this.jobId = jobExecution.getJobId();
    }

    /**
     * Logs the processed data item to igor's log in DEBUG level.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job's execution log.
     *
     * @return Always the supplied data item.
     */
    @Override
    @SuppressWarnings("java:S5247") // We need un-escaped JSON in the logs!
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        var resolvedMessage = Mustache.compiler()
            .escapeHTML(false) // This is required to prevent escaping '"' into '&quot;'
            .withFormatter(o -> {
                if (o instanceof HashMap) {
                    try {
                        return new ObjectMapper().writeValueAsString(o);
                    } catch (JsonProcessingException e) {
                        // Debug level is OK here, because the user will get  the unformatted toString()-value and
                        // processing can go on...
                        log.debug("Logging message appeared to be a JSON-Object but could not be formatted!", e);
                    }
                }
                return o.toString();
            }).compile(message).execute(data);

        if (isSimulation(data)) {
            data.put(DataKey.SIMULATION_LOG.getKey(), "Would have Logged message in loglevel '" + level.toUpperCase() + "'.");
        }

        switch (level) {
            case "error":
                log.error(resolvedMessage);
                break;
            case "warn":
                log.warn(resolvedMessage);
                break;
            case "info":
                log.info(resolvedMessage);
                break;
            case "debug":
                log.debug(resolvedMessage);
                break;
            default:
                log.trace(resolvedMessage);
                break;
        }

        return List.of(data);
    }

}

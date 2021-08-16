package com.arassec.igor.plugin.core.test.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * <h1>Log Action</h1>
 *
 * <h2>Description</h2>
 * This action logs every processed data item to igor's log with DEBUG level.
 */
@Slf4j
@Getter
@Setter
@IgorComponent(typeId = "log-action", categoryId = CoreCategory.TEST)
public class LogAction extends BaseAction {

    /**
     * The job's ID.
     */
    private String jobId;

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
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        if (isSimulation(data)) {
            data.put(DataKey.SIMULATION_LOG.getKey(), "Logged data item in loglevel DEBUG!");
        }
        log.debug("{} - Processed data item:\n{}", jobId, data);
        return List.of(data);
    }

}

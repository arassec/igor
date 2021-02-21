package com.arassec.igor.plugin.core.test.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CorePluginType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Logs the processed data item to igor's log.
 */
@Slf4j
@Getter
@Setter
@IgorComponent
public class LogAction extends BaseTestAction {

    /**
     * The job's ID.
     */
    private String jobId;

    /**
     * Creates a new component instance.
     */
    public LogAction() {
        super(CorePluginType.LOG_ACTION.getId());
    }

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

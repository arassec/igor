package com.arassec.igor.core.model.trigger;

import com.arassec.igor.core.model.BaseIgorComponent;
import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Baseclass for Triggers.
 */
public abstract class BaseTrigger extends BaseIgorComponent implements Trigger {

    /**
     * Stores the {@link JobExecution} to get input for the initial data item.
     */
    private JobExecution jobExecution;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(JobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> createDataItem() {
        Map<String, Object> dataItem = new HashMap<>();

        Map<String, Object> meta = new HashMap<>();
        meta.put(DataKey.JOB_ID.getKey(), jobExecution.getJobId());
        meta.put(DataKey.TIMESTAMP.getKey(), Instant.now().toEpochMilli());
        meta.put(DataKey.SIMULATION.getKey(), false);

        Map<String, Object> data = new HashMap<>();

        dataItem.put(DataKey.META.getKey(), meta);
        dataItem.put(DataKey.DATA.getKey(), data);
        return dataItem;
    }

}

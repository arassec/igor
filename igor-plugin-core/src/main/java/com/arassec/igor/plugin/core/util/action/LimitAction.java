package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

/**
 * Limits the data stream to the first 'n' data items.
 */
@Slf4j
@Getter
@Setter
@IgorComponent
public class LimitAction extends BaseUtilAction {

    /**
     * The configured number of items to skip.
     */
    @Positive
    @IgorParam(defaultValue = "1")
    private int number;

    /**
     * Counts the processed data items.
     */
    private int processed;

    /**
     * Creates a new component instance.
     */
    public LimitAction() {
        super("limit-action");
        // Limiting is always done single threaded:
        setNumThreads(1);
        getUnEditableProperties().add("numThreads");
    }

    /**
     * Limits processing to the configured amount of data items.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job execution log.
     *
     * @return Every data item until the first 'n' have been processed.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        processed++;
        if (processed <= number) {
            return List.of(data);
        }
        return List.of();
    }

    /**
     * Limiting might not work as intended with streams of data items, because the limit is applied to a complete job execution,
     * which is unlimited for streamed data items.
     *
     * @return Always {@code false}.
     */
    @Override
    public boolean supportsEvents() {
        return false;
    }

}

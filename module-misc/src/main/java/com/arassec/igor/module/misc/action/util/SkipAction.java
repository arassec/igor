package com.arassec.igor.module.misc.action.util;

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
 * Skips the first 'n' data items.
 */
@Slf4j
@Getter
@Setter
@IgorComponent
public class SkipAction extends BaseUtilAction {

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
    public SkipAction() {
        super("skip-action");
        // Skipping is always done single threaded:
        setNumThreads(1);
        getUnEditableProperties().add("numThreads");
    }

    /**
     * Skips processing of the configured amount of data items.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job execution log.
     *
     * @return Every data item after the first 'n' have been skipped.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        processed++;
        if (processed <= number) {
            return List.of();
        }
        return List.of(data);
    }

    /**
     * Resets the action so that the configured amount of data items is skiped again.
     */
    @Override
    public void reset() {
        processed = 0;
    }

}

package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CorePluginType;
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
        super(CorePluginType.SKIP_ACTION.getId());
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
     * Skipping items might not work as intended with streams of data items, because the limit is applied to a complete job
     * execution, which is unlimited for streamed data items.
     *
     * @return Always {@code false}.
     */
    @Override
    public boolean supportsEvents() {
        return false;
    }

}

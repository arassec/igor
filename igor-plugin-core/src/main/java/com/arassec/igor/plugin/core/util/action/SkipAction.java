package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreCategory;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * <h2>Skip Action</h2>
 *
 * <h3>Description</h3>
 * This action skips the first 'n' data items.
 *
 * <h3>Event-Triggered Jobs</h3>
 * <strong>This action is not available in event-triggered jobs!</strong>
 *
 * The counter that skips the first 'n' data items is set on job start and applies to the complete job execution. Each skipped
 * data item increases the counter, which is never reset until the next job execution. Since event triggered jobs don't stop their
 * execution, the counter will never be reset.
 *
 * Thus, this action would skip the first 'n' data items of an event-triggered job, and forward <strong>all</strong> following
 * data items afterwards until the job were restarted!
 */
@Slf4j
@Getter
@Setter
@IgorComponent(typeId = "skip-action", categoryId = CoreCategory.UTIL)
public class SkipAction extends BaseUtilAction {

    /**
     * The configured number of items to skip.
     */
    @Positive
    @IgorParam
    private int number = 1;

    /**
     * Counts the processed data items.
     */
    private int processed;

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

    /**
     * Skipping is always done single-threaded.
     *
     * @return Always {@code true}.
     */
    @Override
    public boolean enforceSingleThread() {
        return true;
    }

}

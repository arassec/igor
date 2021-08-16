package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

/**
 * <h1>Limit Action</h1>
 *
 * <h2>Description</h2>
 * This action limits the data stream to the first 'n' data items.
 *
 * <h2>Event-Triggered Jobs</h2>
 * <strong>This action is not available in event-triggered jobs!</strong><br>
 * <p>
 * The limit counter is set on job start and applies to the complete job execution. Each processed data item increases the
 * counter, which is never reset until the next job execution. Since event triggered jobs don't stop their execution, the counter
 * would never be reset.<br>
 * <p>
 * Thus, this action would let the first 'n' data items of an event-triggered job pass, and block **all** following data items
 * until the job were restarted!
 */
@Slf4j
@Getter
@Setter
@IgorComponent(typeId = "limit-action", categoryId = CoreCategory.UTIL)
public class LimitAction extends BaseUtilAction {

    /**
     * The number of data items to limit the stream to.
     */
    @Positive
    @IgorParam
    private int number = 1;

    /**
     * Counts the processed data items.
     */
    private int processed;

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

    /**
     * Limiting is always done single-threaded.
     *
     * @return Always {@code true}.
     */
    @Override
    public boolean enforceSingleThread() {
        return true;
    }

}

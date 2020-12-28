package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.core.model.DataKey;
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
 * Pauses the processing for a configurable amount of time.
 */
@Slf4j
@Getter
@Setter
@IgorComponent
public class PauseAction extends BaseUtilAction {

    /**
     * Amount of milliseconds the action should pause data processing.
     */
    @Positive
    @IgorParam(defaultValue = "1000")
    private long milliseconds;

    /**
     * Creates a new component instance.
     */
    public PauseAction() {
        super("pause-action");
    }

    /**
     * Pauses the processing for every data that is supplied to it.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job execution log.
     *
     * @return The supplied data item.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        if (isSimulation(data)) {
            data.put(DataKey.SIMULATION_LOG.getKey(), "Would have paused for " + milliseconds + " milliseconds.");
        } else {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                log.debug("Interrupted during pause action!", e);
                Thread.currentThread().interrupt();
            }
        }
        return List.of(data);
    }

}

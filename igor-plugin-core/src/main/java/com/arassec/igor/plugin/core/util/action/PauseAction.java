package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * <h1>Pause Action</h1>
 *
 * <h2>Description</h2>
 * This action pauses execution by the configured amount of time. Execution pauses <strong>for every data item</strong> which is
 * processed by the action.
 */
@Slf4j
@Getter
@Setter
@IgorComponent(typeId = "pause-action", categoryId = CoreCategory.UTIL)
public class PauseAction extends BaseAction {

    /**
     * The number of milliseconds to pause for each data item.
     */
    @Positive
    @IgorParam
    private int milliseconds = 1000;

    /**
     * A number smaller than 'Milliseconds'. If set, the actual pause time will be random and between 'Milliseconds - Variance'
     * and 'Milliseconds + Variance'. Set to 0 to disable variable pause times.
     */
    @PositiveOrZero
    @IgorParam(advanced = true)
    private int variance = 0;

    /**
     * Random number generator for including variance in the pause.
     */
    private Random random;

    /**
     * Creates a new component instance.
     */
    public PauseAction() {
        random = new SecureRandom();
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
        int waitTime = milliseconds;
        if (variance > 0 && variance < milliseconds) {
            int max = milliseconds + variance;
            int min = milliseconds - variance;
            waitTime = random.nextInt((max - min) + 1) + min;
        }

        if (isSimulation(data)) {
            data.put(DataKey.SIMULATION_LOG.getKey(), "Would have paused for " + waitTime + " milliseconds.");
        } else {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                log.debug("Interrupted during pause action!", e);
                Thread.currentThread().interrupt();
            }
        }
        return List.of(data);
    }

}

package com.arassec.igor.plugin.core.test.action;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
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
 * Pauses the processing for a configurable amount of time.
 */
@Slf4j
@Getter
@Setter
@IgorComponent
public class PauseAction extends BaseTestAction {

    /**
     * Amount of milliseconds the action should pause data processing.
     */
    @Positive
    @IgorParam(defaultValue = "1000")
    private int milliseconds;

    /**
     * Causes the pause to vary for each data item.
     */
    @PositiveOrZero
    @IgorParam(advanced = true, defaultValue = "0")
    private int variance;

    /**
     * Random number generator for including variance in the pause.
     */
    private Random random;

    /**
     * Creates a new component instance.
     */
    public PauseAction() {
        super("pause-action");
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

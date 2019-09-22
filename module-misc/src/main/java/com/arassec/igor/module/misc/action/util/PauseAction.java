package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Pauses the processing for a configurable amount of time.
 */
@IgorComponent("Pause")
@Slf4j
public class PauseAction extends BaseUtilAction {

    /**
     * Amount of milliseconds the action should pause data processing.
     */
    @IgorParam
    private long milliseconds;

    /**
     * Pauses the processing for every data that is supplied to it.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job execution log.
     *
     * @return Always {@code true}.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            log.debug("Interrupted during pause action!");
        }
        return List.of(data);
    }

}

package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Pauses the processing for a configurable amount of time.
 */
@IgorAction(label = "Pause")
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
     * @param data The data the action will work with.
     * @return Always {@code true}.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, boolean isDryRun) {
        if (isDryRun) {
            data.put(DRY_RUN_COMMENT_KEY, "Sleep for " + milliseconds + " milliseconds.");
        } else {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                log.debug("Interrupted during pause action!");
            }
        }
        return List.of(data);
    }

}

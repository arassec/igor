package com.arassec.igor.core.model.action.misc;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.provider.IgorData;
import lombok.extern.slf4j.Slf4j;

/**
 * Pauses the processing for a configurable amount of time.
 */
@IgorAction(label = "Pause")
@Slf4j
public class PauseAction extends BaseMiscAction {

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
    public boolean process(IgorData data) {
        return processInternal(data, false);
    }

    /**
     * Adds a comment to the data in which the amount of milliseconds is contained.
     * <p>
     * Does not actually pause the processing!
     *
     * @param data The data the action will work with.
     * @return Always {@code true}.
     */
    @Override
    public boolean dryRun(IgorData data) {
        return processInternal(data, true);
    }

    /**
     * Either pauses the processing of the data or adds a comment to the data.
     *
     * @param data     The data the action will work with.
     * @param isDryRun Set to {@code true}, if the procssing should not actually be paused. A comment is added to the
     *                 data in this case.
     * @return Always {@code true}.
     */
    private boolean processInternal(IgorData data, boolean isDryRun) {
        if (isDryRun) {
            data.put(DRY_RUN_COMMENT_KEY, "Sleep for " + milliseconds + " milliseconds.");
        } else {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                log.debug("Interrupted during pause action!");
            }
        }
        return true;
    }

}

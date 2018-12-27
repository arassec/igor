package com.arassec.igor.core.model.action.misc;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.provider.IgorData;
import lombok.extern.slf4j.Slf4j;

@IgorAction(label = "Pause")
@Slf4j
public class PauseAction extends BaseAction {

    @IgorParam
    private long milliseconds;

    @Override
    public boolean process(IgorData data) {
        return processInternal(data, false);
    }

    @Override
    public boolean dryRun(IgorData data) {
        return processInternal(data, true);
    }

    private boolean processInternal(IgorData data, boolean isDryRun) {
        if (isDryRun) {
            data.put("dryRunComment", "Sleep for " + milliseconds + " milliseconds.");
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

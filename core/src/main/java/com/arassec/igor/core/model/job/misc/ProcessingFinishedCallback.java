package com.arassec.igor.core.model.job.misc;

import java.util.Map;

/**
 * Defines a callback that is invoked after an action processed a data item.
 */
public interface ProcessingFinishedCallback {

    /**
     * Called by the job if an action has the callback set and finishes processing a data item.
     *
     * @param dataItem The processed data item.
     */
    default void processingFinished(Map<String, Object> dataItem) {
    }

}

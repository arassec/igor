package com.arassec.igor.core.model.job.execution;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Monitors work in progress that is executed.
 */
@Data
@RequiredArgsConstructor
public class WorkInProgressMonitor {

    /**
     * The name of the work.
     */
    private final String name;

    /**
     * The progress in percent.
     */
    private double progressInPercent = 0;

}

package com.arassec.igor.core.model.job.execution;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Monitors work in progress that is executed.
 */
@Data
@AllArgsConstructor
public class WorkInProgressMonitor {

    /**
     * The name of the work.
     */
    private String name;

    /**
     * The progress in percent.
     */
    private double progressInPercent;

}

package com.arassec.igor.core.model.job.starter;

import com.arassec.igor.core.model.job.concurrent.ConcurrencyGroup;

import java.util.List;

/**
 * Starts processing the data items of a job.
 */
public interface JobStarter {

    /**
     * Starts processing the data items of a job.
     *
     * @return List of {@link ConcurrencyGroup}s that is used for processing the job's data items.
     */
    List<ConcurrencyGroup> process();

}

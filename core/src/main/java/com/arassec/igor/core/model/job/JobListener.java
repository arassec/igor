package com.arassec.igor.core.model.job;

/**
 * Defines the interface for classes that want to be informed about a job's lifecycle.
 */
public interface JobListener {

    /**
     * Will be called by a job if it starts a new run.
     *
     * @param job The job starting the run.
     */
    void notifyStarted(Job job);

    /**
     * Will be called by a job if it finished a run.
     *
     * @param job The job that finished the run.
     */
    void notifyFinished(Job job);

}

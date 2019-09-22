package com.arassec.igor.core.application;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;

import java.util.concurrent.Callable;

/**
 * Callable that runs the supplied job and returns it after execution.
 */
public class JobRunningCallable implements Callable<Job> {

    /**
     * The job to run.
     */
    private Job job;

    /**
     * The job's execution.
     */
    private JobExecution jobExecution;

    /**
     * Creates a new JobRunningCallable instance.
     *
     * @param job          The job.
     * @param jobExecution The job's execution.
     */
    public JobRunningCallable(Job job, JobExecution jobExecution) {
        this.job = job;
        this.jobExecution = jobExecution;
    }

    /**
     * Runs the job.
     *
     * @return The job after its execution.
     */
    @Override
    public Job call() {
        job.run(jobExecution);
        return job;
    }

}

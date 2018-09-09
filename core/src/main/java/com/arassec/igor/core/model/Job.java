package com.arassec.igor.core.model;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Defines an igor job.
 */
@Data
public class Job {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Job.class);

    /**
     * The job's ID.
     */
    private Long id;

    /**
     * The job's name.
     */
    private String name;

    /**
     * A job description.
     */
    private String description;

    /**
     * The cron-expression that defines when the job is triggered.
     */
    private String trigger;

    /**
     * Indicates if this job is active or not.
     */
    private boolean active;

    /**
     * The tasks this job will perform.
     */
    private List<Task> tasks = new LinkedList<>();

    /**
     * Runs the job.
     */
    public void run() {
        LOG.debug("Running job: {}", getId());
        for (Task task : tasks) {
            task.run(name);
        }
        LOG.debug("Finished job: {}", getId());
    }

}

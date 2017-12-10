package com.arassec.igor.core.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Defines an igor job.
 * <p>
 * Created by sensen on 3/26/17.
 */
public class Job {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Job.class);

    /**
     * The job's ID.
     */
    private String id;

    /**
     * A job description.
     */
    private String description;

    /**
     * The cron-expression that defines when the job is triggered.
     */
    private String trigger;

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
            task.run(id);
        }
        LOG.debug("Finished job: {}", getId());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

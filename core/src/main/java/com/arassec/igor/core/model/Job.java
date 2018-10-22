package com.arassec.igor.core.model;

import com.arassec.igor.core.model.provider.IgorData;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        LOG.debug("Running job: {} ({})", name, id);
        for (Task task : tasks) {
            task.run(name);
        }
        LOG.debug("Finished job: {} ({})", name, id);
    }

    /**
     * Performs a dry-run of the job.
     *
     * @return The result data created during job execution.
     */
    public Map<String, Object> dryRun() {
        LOG.debug("Dry-running job: {}", name);
        Map<String, Object> dryRunResults = new HashMap<>();
        List<List<IgorData>> taskResults = new LinkedList<>();
        dryRunResults.put("taskResults", taskResults);
        tasks.stream().forEach(task -> task.dryRun(dryRunResults, name));
        LOG.debug("Finished dry-run of job: {}", name);
        return dryRunResults;
    }

}

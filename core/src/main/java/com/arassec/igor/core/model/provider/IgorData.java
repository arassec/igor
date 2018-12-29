package com.arassec.igor.core.model.provider;

import java.util.HashMap;
import java.util.StringJoiner;

/**
 * Data that is created by providers and processed by Actions.
 * <p>
 * This is basically a JSON-Object with additional runtime data like the job's ID and the current task's name.
 */
public class IgorData extends HashMap<String, Object> {

    /**
     * The job's ID.
     */
    private String jobId;

    /**
     * The task's name.
     */
    private String taskName;

    /**
     * Creates a new IgorData.
     *
     * @param jobId    The job's ID.
     * @param taskName The task's name.
     */
    public IgorData(String jobId, String taskName) {
        super();
        this.jobId = jobId;
        this.taskName = taskName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("IgorData[");
        StringJoiner sj = new StringJoiner(",");
        entrySet().stream().forEach(entry -> sj.add(entry.getKey() + ":" + entry.getValue()));
        sb.append(sj.toString());
        sb.append("]");
        return sb.toString();
    }

    /**
     * Returns the job's ID.
     *
     * @return The job's ID.
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * Returns the task's name.
     *
     * @return The task's name.
     */
    public String getTaskName() {
        return taskName;
    }
}

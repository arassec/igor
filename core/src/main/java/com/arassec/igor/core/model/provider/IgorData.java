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
    private Long jobId;

    /**
     * The task's ID.
     */
    private String taskId;

    /**
     * Creates a new IgorData.
     *
     * @param jobId  The job's ID.
     * @param taskId The task's ID.
     */
    public IgorData(Long jobId, String taskId) {
        super();
        this.jobId = jobId;
        this.taskId = taskId;
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
    public Long getJobId() {
        return jobId;
    }

    /**
     * Returns the task's ID.
     *
     * @return The task's ID.
     */
    public String getTaskId() {
        return taskId;
    }
}

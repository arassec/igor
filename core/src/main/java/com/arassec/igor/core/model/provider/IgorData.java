package com.arassec.igor.core.model.provider;

import java.util.HashMap;
import java.util.StringJoiner;

/**
 * Data that is created by providers and processed by Actions.
 */
public class IgorData extends HashMap<String, Object> {

    private String jobId;

    private String taskName;

    public IgorData(String jobId, String taskName) {
        super();
        this.jobId = jobId;
        this.taskName = taskName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("IgorData[");
        StringJoiner sj = new StringJoiner(",");
        entrySet().stream().forEach(entry -> sj.add(entry.getKey() + ":" + entry.getValue()));
        sb.append(sj.toString());
        sb.append("]");
        return sb.toString();
    }

    public String getJobId() {
        return jobId;
    }

    public String getTaskName() {
        return taskName;
    }
}

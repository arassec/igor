package com.arassec.igor.core.application.converter.simulation;

import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.util.StacktraceFormatter;
import com.github.openjson.JSONObject;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Proxy for {@link Action}s that collects the result data of the action and stores it for further processing.
 */
@Data
public class ActionProxy implements Action {

    /**
     * The real action this proxy is hiding.
     */
    private Action delegate;

    /**
     * The collected Data.
     */
    private List<JSONObject> collectedData = new LinkedList<>();

    /**
     * Might contain an error cause if the action finished abnormally.
     */
    private String errorCause;

    /**
     * Creates a new proxy instance.
     *
     * @param delegate The proxied action.
     */
    public ActionProxy(Action delegate) {
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        try {
            delegate.initialize();
        } catch (Exception e) {
            errorCause = StacktraceFormatter.format(e);
        }
    }

    /**
     * Collects all data returned by the proxied action when {@link Action#process(Map, JobExecution)} is called.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job's execution log.
     *
     * @return The processed data.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        try {
            List<Map<String, Object>> resultData = delegate.process(data, jobExecution);
            if (resultData != null) {
                collectedData.add(new JSONObject(data));
            }
            return resultData;
        } catch (Exception e) {
            errorCause = StacktraceFormatter.format(e);
        }
        return null;
    }

    /**
     * Collects all data returned by the proxied action when {@link Action#complete()} is called.
     *
     * @return The collected data.
     */
    @Override
    public List<Map<String, Object>> complete() {
        try {
            List<Map<String, Object>> resultData = delegate.complete();
            if (resultData != null && !resultData.isEmpty()) {
                resultData.stream().forEach(data -> collectedData.add(new JSONObject(data)));
            }
            return resultData;
        } catch (Exception e) {
            errorCause = StacktraceFormatter.format(e);
        }
        return List.of();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown(Long jobId, String taskId) {
        try {
            delegate.shutdown(jobId, taskId);
        } catch (Exception e) {
            errorCause = StacktraceFormatter.format(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumThreads() {
        return delegate.getNumThreads();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return delegate.isActive();
    }

}

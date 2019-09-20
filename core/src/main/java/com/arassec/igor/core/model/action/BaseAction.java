package com.arassec.igor.core.model.action;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;

import java.util.List;
import java.util.Map;

/**
 * Base action that implements common functionality of an action. Specific actions should be derived from this class.
 */
public abstract class BaseAction implements Action {

    /**
     * Contains the default number of threads this action should be executed with.
     */
    public static final int DEFAULT_THREADS = 1;

    /**
     * Dummy work-in-progress monitor that can be used, if progress shouldn't be monitored.
     */
    protected static final WorkInProgressMonitor VOID_WIP_MONITOR = new WorkInProgressMonitor("", 0);

    /**
     * Activates or deactivates an action.
     */
    protected boolean active = true;

    /**
     * Key into the data map that identifies the property to process.
     */
    @IgorParam
    protected String dataKey = "data";

    /**
     * Defines the number of threads the action should be processed with.
     */
    @IgorParam
    protected int numThreads = DEFAULT_THREADS;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        // Nothing to do here...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> complete() {
        // Nothing to do here...
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown(Long jobId, String taskName) {
        // Nothing to do here...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumThreads() {
        return numThreads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return active;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns whether the supplied data is valid or not.
     *
     * @param data The data to validate.
     * @return {@code true} if the data is valid for processing, {@code false} otherwise.
     */
    protected boolean isValid(Map<String, Object> data) {
        if (getLong(data, JOB_ID_KEY) == null) {
            return false;
        }
        if (getString(data, TASK_ID_KEY) == null) {
            return false;
        }
        return getString(data, dataKey) != null;
    }

    /**
     * Returns the value of the specified key as String.
     *
     * @param data The data.
     * @param key  The key to get the value for.
     * @return The key's value as String or {@code null}, if no value exists.
     */
    protected String getString(Map<String, Object> data, String key) {
        if (data.containsKey(key) && data.get(key) instanceof String) {
            return (String) data.get(key);
        }
        return null;
    }

    /**
     * Returns the value of the specified key as Long.
     *
     * @param data The data.
     * @param key  The key to get the value for.
     * @return The key's value as Long or {@code null}, if no value exists.
     */
    protected Long getLong(Map<String, Object> data, String key) {
        if (data.containsKey(key) && data.get(key) instanceof Long) {
            return (Long) data.get(key);
        }
        return null;
    }

}

package com.arassec.igor.core.model.action;

import com.arassec.igor.core.model.BaseIgorComponent;
import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.misc.ProcessingFinishedCallback;

import java.util.Map;

/**
 * Base action that implements common functionality of an action. Specific actions should be derived from this class.
 */
public abstract class BaseAction extends BaseIgorComponent implements Action {

    /**
     * Activates or deactivates an action.
     */
    protected boolean active = true;

    /**
     * The action's optional name.
     */
    private String name;

    /**
     * A description for the action.
     */
    private String description;

    /**
     * Callback to call when processing of a data item finished.
     */
    private ProcessingFinishedCallback processingFinishedCallback;

    /**
     * Creates a new component instance.
     *
     * @param categoryId The category ID.
     * @param typeId     The type ID.
     */
    protected BaseAction(String categoryId, String typeId) {
        super(categoryId, typeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
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
     * {@inheritDoc}
     */
    @Override
    public void setProcessingFinishedCallback(ProcessingFinishedCallback processingFinishedCallback) {
        this.processingFinishedCallback = processingFinishedCallback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessingFinishedCallback getProcessingFinishedCallback() {
        return processingFinishedCallback;
    }

    /**
     * Returns the current job's ID.
     *
     * @param data The data to process.
     *
     * @return The job's ID.
     */
    @SuppressWarnings("unchecked")
    protected String getJobId(Map<String, Object> data) {
        if (data != null
            && !data.isEmpty()
            && data.containsKey(DataKey.META.getKey())
            && ((Map<String, Object>) data.get(DataKey.META.getKey())).containsKey(DataKey.JOB_ID.getKey())) {
            var jobId = String.valueOf(((Map<String, Object>) data.get(DataKey.META.getKey())).get(DataKey.JOB_ID.getKey()));
            if (jobId != null) {
                return jobId;
            }
        }
        throw new IllegalStateException("No Job-ID found in meta-data!");
    }

    /**
     * Indicates whether a simulated job run is in progress, or a real one.
     *
     * @param data The data to process.
     *
     * @return {@code true} if the data is processed during a simulated job run, {@code false} otherwise.
     */
    @SuppressWarnings("unchecked")
    protected boolean isSimulation(Map<String, Object> data) {
        if (data == null
            || data.isEmpty()
            || !data.containsKey(DataKey.META.getKey())
            || !((Map<String, Object>) data.get(DataKey.META.getKey())).containsKey(DataKey.SIMULATION.getKey())) {
            return false;
        }

        var result = String.valueOf(((Map<String, Object>) data.get(DataKey.META.getKey())).get(DataKey.SIMULATION.getKey()));

        if (result == null) {
            return false;
        }

        return Boolean.parseBoolean(result);
    }

}

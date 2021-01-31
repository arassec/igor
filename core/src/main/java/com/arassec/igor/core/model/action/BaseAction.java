package com.arassec.igor.core.model.action;

import com.arassec.igor.core.model.BaseIgorComponent;
import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.misc.ProcessingFinishedCallback;
import com.jayway.jsonpath.JsonPath;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.MustacheException;

import javax.validation.constraints.Positive;
import java.util.Map;

/**
 * Base action that implements common functionality of an action. Specific actions should be derived from this class.
 */
public abstract class BaseAction extends BaseIgorComponent implements Action {

    /**
     * Template for the Job-ID.
     */
    private static final String JOB_ID_TEMPLATE = "{{" + DataKey.META.getKey() + "." + DataKey.JOB_ID.getKey() + "}}";

    /**
     * Template for the simulation property that indicates a simulated job run.
     */
    private static final String SIMULATION_TEMPLATE = "{{" + DataKey.META.getKey() + "." + DataKey.SIMULATION.getKey() + "}}";

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
     * Defines the number of threads the action should be processed with.
     */
    @Positive
    @IgorParam(value = Integer.MAX_VALUE, advanced = true, defaultValue = "1")
    protected int numThreads;

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
    public int getNumThreads() {
        return numThreads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
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
    protected String getJobId(Map<String, Object> data) {
        if (data != null && !data.isEmpty()) {
            String jobId = getString(data, JOB_ID_TEMPLATE);
            if (jobId != null && !JOB_ID_TEMPLATE.equals(jobId)) {
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
    protected boolean isSimulation(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return false;
        }

        String result = getString(data, SIMULATION_TEMPLATE);

        if (result == null || SIMULATION_TEMPLATE.equals(result)) {
            return false;
        }

        return Boolean.parseBoolean(result);
    }

    /**
     * Returns the result of the template executed against the provided data. If the template is invalid with regard to the input
     * data, it is returned without modifications.
     *
     * @param data     The data to execute the query on.
     * @param template The mustache template.
     *
     * @return The template executed against the data or the original template if either of the input parameters is {@code null}
     * or the template is invalid.
     */
    protected String getString(Map<String, Object> data, String template) {
        if (data == null || template == null) {
            return null;
        }
        try {
            return Mustache.compiler().compile(template).execute(data);
        } catch (MustacheException e) {
            return template;
        }
    }

    /**
     * Creates a deep copy of the supplied map.
     *
     * @param data The map to clone.
     *
     * @return A new Map instance with copied content.
     */
    protected Map<String, Object> clone(Map<String, Object> data) {
        return JsonPath.parse(JsonPath.parse(data).jsonString()).json();
    }

}

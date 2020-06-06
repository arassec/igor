package com.arassec.igor.core.model.action;

import com.arassec.igor.core.model.BaseIgorComponent;
import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

/**
 * Base action that implements common functionality of an action. Specific actions should be derived from this class.
 */
public abstract class BaseAction extends BaseIgorComponent implements Action {

    /**
     * The default JSON-Path configuration.
     */
    static final Configuration DEFAULT_JSONPATH_CONFIG = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS,
            Option.DEFAULT_PATH_LEAF_TO_NULL);

    /**
     * Query for the Job-ID.
     */
    private static final String JOB_ID_QUERY = "$." + DataKey.META.getKey() + "." + DataKey.JOB_ID.getKey();

    /**
     * Query for the Task-ID.
     */
    private static final String TASK_ID_QUERY = "$." + DataKey.META.getKey() + "." + DataKey.TASK_ID.getKey();

    /**
     * Query for the simulation property that indicates a simulated job run.
     */
    private static final String SIMULATION_QUERY = "$." + DataKey.DATA.getKey() + "." + DataKey.SIMULATION.getKey();

    /**
     * Dummy work-in-progress monitor that can be used, if progress shouldn't be monitored.
     */
    protected static final WorkInProgressMonitor VOID_WIP_MONITOR = new WorkInProgressMonitor();

    /**
     * Activates or deactivates an action.
     */
    protected boolean active = true;

    /**
     * The action's optional name.
     */
    private String name;

    /**
     * Defines the number of threads the action should be processed with.
     */
    @Positive
    @IgorParam(advanced = true, defaultValue = "1")
    protected int numThreads;

    /**
     * The JSON-Path configuration.
     */
    @Getter
    @Setter
    private Configuration jsonPathConfiguration = DEFAULT_JSONPATH_CONFIG;

    /**
     * Creates a new component instance.
     *
     * @param categoryId The category ID.
     * @param typeId     The type ID.
     */
    public BaseAction(String categoryId, String typeId) {
        super(categoryId, typeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> complete() {
        // Nothing to do here...
        return List.of();
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
     * Returns the current job's ID.
     *
     * @param data The data to process.
     *
     * @return The job's ID.
     */
    protected String getJobId(Map<String, Object> data) {
        if (data != null && !data.isEmpty()) {
            String jobId = getString(data, JOB_ID_QUERY);
            if (jobId != null) {
                return jobId;
            }
        }
        throw new IllegalStateException("No Job-ID found in meta-data!");
    }

    /**
     * Returns the current task's ID.
     *
     * @param data The data to process.
     *
     * @return The current task's ID.
     */
    protected String getTaskId(Map<String, Object> data) {
        if (data != null && !data.isEmpty()) {
            String taskId = getString(data, TASK_ID_QUERY);
            if (taskId != null) {
                return taskId;
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

        Boolean value = JsonPath.using(jsonPathConfiguration).parse(data).read(BaseAction.SIMULATION_QUERY);
        if (value != null) {
            return value;
        }

        return false;
    }

    /**
     * Determines whether the supplied String is a JSON-Path query or not.
     *
     * @param query The query to test.
     *
     * @return {@code true} if the supplied String can be used as JSON-Path query, {@code false} otherwise.
     */
    protected boolean isQuery(String query) {
        if (query == null || query.isEmpty()) {
            return false;
        }
        return query.startsWith("$");
    }

    /**
     * Returns the result of the JSON-Path query against the provided data. If no valid JSON-Path query is provided as input, the
     * query is returned instead.
     *
     * @param data  The data to execute the query on.
     * @param query The JSON-Path query.
     *
     * @return The querie's result or the query itself, if it isn't a JSON-Path query.
     */
    protected String getString(Map<String, Object> data, String query) {
        if (data == null || query == null) {
            return null;
        }

        if (!isQuery(query)) {
            return query;
        }

        Object result = JsonPath.using(jsonPathConfiguration).parse(data).read(query);

        if (result instanceof String) {
            return (String) result;
        } else if (result != null) {
            return String.valueOf(result);
        }

        return null;
    }

}

package com.arassec.igor.core.model.action;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import javax.validation.constraints.Min;
import java.util.List;
import java.util.Map;

/**
 * Base action that implements common functionality of an action. Specific actions should be derived from this class.
 */
public abstract class BaseAction implements Action {

    /**
     * Query for the Job-ID.
     */
    private static final String JOB_ID_QUERY = "$.meta.jobId";

    /**
     * Query for the Task-ID.
     */
    private static final String TASK_ID_QUERY = "$.meta.taskId";

    /**
     * Query for the simulation property that indicates a simulated job run.
     */
    private static final String SIMULATION_QUERY = "$.data.simulation";

    /**
     * The key for simulation log entries.
     */
    protected static final String SIMULATION_LOG_KEY = "simulationLog";

    /**
     * Dummy work-in-progress monitor that can be used, if progress shouldn't be monitored.
     */
    protected static final WorkInProgressMonitor VOID_WIP_MONITOR = new WorkInProgressMonitor("", 0);

    /**
     * Activates or deactivates an action.
     */
    protected boolean active = true;

    /**
     * Defines the number of threads the action should be processed with.
     */
    @Min(1)
    @IgorParam
    protected int numThreads = 1;

    /**
     * The JSON-Path configuration.
     */
    private Configuration jsonPathConfiguration = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS,
            Option.DEFAULT_PATH_LEAF_TO_NULL);

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
     * Returns the current job's ID.
     *
     * @param data The data to process.
     *
     * @return The job's ID.
     */
    protected Long getJobId(Map<String, Object> data) {
        if (data != null && !data.isEmpty()) {
            Long jobId = getLong(data, JOB_ID_QUERY);
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
        return getBoolean(data, SIMULATION_QUERY);
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

        if (!query.startsWith("$")) {
            return query;
        }

        return JsonPath.using(jsonPathConfiguration).parse(data).read(query);
    }

    /**
     * Executes the provided JSON-Path query against the supplied data and returns the result as Boolean.
     *
     * @param data  The data to execute the query on.
     * @param query The JSON-Path query.
     *
     * @return The querie's result if any, or {@code false} in every other case.
     */
    protected boolean getBoolean(Map<String, Object> data, String query) {
        if (data == null || query == null) {
            return false;
        }

        if (!query.startsWith("$")) {
            return false;
        }

        Boolean value = JsonPath.using(jsonPathConfiguration).parse(data).read(query);
        if (value != null) {
            return value;
        }

        return false;
    }

    /**
     * Returns a long value from the supplied data using the supplied query.
     *
     * @param data  The data to execute the query on.
     * @param query The JSON-Path query.
     *
     * @return The querie's result or {@code null}, if no data could be retrieved.
     */
    protected Long getLong(Map<String, Object> data, String query) {
        if (data == null || query == null) {
            return null;
        }

        if (!query.startsWith("$")) {
            return null;
        }

        return JsonPath.using(jsonPathConfiguration).parse(data).read(query);
    }

}

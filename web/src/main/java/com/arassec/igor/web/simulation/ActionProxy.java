package com.arassec.igor.web.simulation;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.util.StacktraceFormatter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Proxy for {@link Action}s that collects the result data of the action and stores it for further processing.
 */
@Getter
@Setter
public class ActionProxy extends BaseProxy<Action> implements Action {

    /**
     * The collected Data.
     */
    private List<Map<String, Object>> collectedData = new LinkedList<>();

    /**
     * Object-Mapper to create deep copies of processed data.
     */
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Keeps track of the number of processed data items to limit them according to the simulation limit configuration.
     */
    private int processed;

    /**
     * Creates a new proxy instance.
     *
     * @param delegate The proxied action.
     */
    public ActionProxy(Action delegate) {
        super(delegate);
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

            int simulationLimit = -1;
            processed++;
            if (data.containsKey(DataKey.META.getKey())) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metaData = (Map<String, Object>) data.get(DataKey.META.getKey());
                if (metaData.containsKey(DataKey.SIMULATION_LIMIT.getKey())) {
                    simulationLimit = (int) metaData.get(DataKey.SIMULATION_LIMIT.getKey());
                    if (processed > simulationLimit) {
                        return List.of();
                    }
                }
            }

            data.remove(DataKey.SIMULATION_LOG.getKey());
            List<Map<String, Object>> resultData = delegate.process(data, jobExecution);

            if (simulationLimit > 0 && resultData.size() > simulationLimit) {
                resultData = resultData.subList(0, simulationLimit);
            }

            collectedData.addAll(objectMapper.readValue(objectMapper.writeValueAsString(resultData), new TypeReference<>() {
            }));

            return resultData;
        } catch (Exception e) {
            setErrorCause(StacktraceFormatter.format(e));
        }
        return List.of();
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
            if (resultData != null) {
                collectedData.addAll(objectMapper.readValue(objectMapper.writeValueAsString(resultData), new TypeReference<>() {
                }));
            }
            return resultData;
        } catch (Exception e) {
            setErrorCause(StacktraceFormatter.format(e));
        }
        return List.of();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return delegate.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(String name) {
        delegate.setName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDescription(String description) {
        delegate.setDescription(description);
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
    public void setNumThreads(int numThreads) {
        delegate.setNumThreads(numThreads);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return delegate.isActive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActive(boolean active) {
        delegate.setActive(active);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        processed = 0;
        delegate.reset();
    }

}

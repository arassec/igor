package com.arassec.igor.simulation.job.proxy;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ProcessingFinishedCallback;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.core.util.StacktraceFormatter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Proxy for {@link Action}s that collects the result data of the action and stores it for further processing.
 */
@Getter
@Setter
@Slf4j
public class ActionProxy extends BaseProxy<Action> implements Action {

    /**
     * The collected Data.
     */
    private List<Map<String, Object>> collectedData = Collections.synchronizedList(new LinkedList<>());

    /**
     * Object-Mapper to create deep copies of processed data.
     */
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a new proxy instance.
     *
     * @param delegate        The proxied action.
     * @param simulationLimit The maximum number of data items that should be processed in a simulated job execution.
     */
    public ActionProxy(Action delegate, int simulationLimit) {
        super(delegate, simulationLimit);
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
            processed++;
            if (processed > simulationLimit) {
                return List.of();
            }

            data.remove(DataKey.SIMULATION_LOG.getKey());
            List<Map<String, Object>> resultData = delegate.process(data, jobExecution);

            if (simulationLimit > 0 && resultData.size() > simulationLimit) {
                resultData = resultData.subList(0, simulationLimit);
            }

            // Creates a deep copy of the collected data items:
            resultData.forEach(resultDataItem -> {
                try {
                    collectedData.add(objectMapper.readValue(objectMapper.writeValueAsString(resultDataItem), new TypeReference<>() {
                    }));
                } catch (JsonProcessingException e) {
                    throw new IgorException("Error during job simulation!", e);
                }
            });

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
                // Creates a deep copy of the collected data items:
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
    public void setProcessingFinishedCallback(ProcessingFinishedCallback processingFinishedCallback) {
        delegate.setProcessingFinishedCallback(processingFinishedCallback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessingFinishedCallback getProcessingFinishedCallback() {
        return delegate.getProcessingFinishedCallback();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsEvents() {
        return delegate.supportsEvents();
    }

}

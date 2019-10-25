package com.arassec.igor.web.simulation;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.util.StacktraceFormatter;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Proxy for {@link Provider}s, that collects the provided data for later processing.
 */
@Data
public class ProviderProxy implements Provider {

    /**
     * The real provider proxied by this class.
     */
    private Provider delegate;

    /**
     * The limit of data items that should be returned by the simulation run.
     */
    private int simulationLimit;

    /**
     * Contains the number of data items that were actually returned by this proxy so far.
     */
    private int curItemsProcessed = 0;

    /**
     * The collected data.
     */
    private List<Map<String, Object>> collectedData = new LinkedList<>();

    /**
     * Might contain an error cause if the proxied provider failed abnormally.
     */
    private String errorCause;

    /**
     * Creates a new proxy instance.
     *
     * @param delegate The real provider proxied by this instance.
     */
    public ProviderProxy(Provider delegate) {
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(String jobId, String taskId, JobExecution jobExecution) {
        try {
            delegate.initialize(jobId, taskId, jobExecution);
        } catch (Exception e) {
            errorCause = StacktraceFormatter.format(e);
        }
    }

    /**
     * Returns {@code true}, if the {@link #simulationLimit} has not been hit and the proxied provider has more data to provide.
     *
     * @return {@code true} if more data is available, {@code false} otherwise.
     */
    @Override
    public boolean hasNext() {
        try {
            if (curItemsProcessed < delegate.getSimulationLimit()) {
                curItemsProcessed++;
                return delegate.hasNext();
            }
        } catch (Exception e) {
            errorCause = StacktraceFormatter.format(e);
        }
        return false;
    }

    /**
     * Collects the next data item provided by the proxied provider and returns it.
     *
     * @return The next data item.
     */
    @Override
    public Map<String, Object> next() {
        try {
            Map<String, Object> data = delegate.next();
            if (data != null) {
                collectedData.add(data);
            }
            return data;
        } catch (Exception e) {
            errorCause = StacktraceFormatter.format(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown(String jobId, String taskId, JobExecution jobExecution) {
        try {
            delegate.shutdown(jobId, taskId, jobExecution);
        } catch (Exception e) {
            errorCause = StacktraceFormatter.format(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCategoryId() {
        return delegate.getCategoryId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeId() {
        return delegate.getTypeId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return delegate.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(String id) {
        delegate.setId(id);
    }

}

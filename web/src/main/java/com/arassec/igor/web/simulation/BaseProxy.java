package com.arassec.igor.web.simulation;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.util.StacktraceFormatter;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base class for {@link IgorComponent} proxies.
 *
 * @param <T> The igor component type proxied by this class.
 */
@Getter
@Setter
public abstract class BaseProxy<T extends IgorComponent> implements IgorComponent {

    /**
     * The original {@link IgorComponent}.
     */
    protected T delegate;

    /**
     * Might contain an error cause if the proxied provider failed abnormally.
     */
    private String errorCause;

    /**
     * Creates a new instance.
     *
     * @param delegate The original {@link IgorComponent}.
     */
    BaseProxy(T delegate) {
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(String jobId, JobExecution jobExecution) {
        try {
            delegate.initialize(jobId, jobExecution);
        } catch (Exception e) {
            setErrorCause(StacktraceFormatter.format(e));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown(String jobId, JobExecution jobExecution) {
        try {
            delegate.shutdown(jobId, jobExecution);
        } catch (Exception e) {
            errorCause = StacktraceFormatter.format(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getUnEditableProperties() {
        return delegate.getUnEditableProperties();
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

    /**
     * Marks data items as "in simulation mode".
     *
     * @return Meta-data for the data items.
     */
    protected Map<String, Object> createMetaData(Map<String, Object> delegateMetaData) {
        Map<String, Object> metaData = new HashMap<>();
        metaData.put(DataKey.SIMULATION.getKey(), true);
        if (delegateMetaData != null) {
            delegateMetaData.forEach(metaData::put);
        }
        return metaData;
    }

}

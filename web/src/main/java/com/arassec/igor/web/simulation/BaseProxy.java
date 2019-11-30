package com.arassec.igor.web.simulation;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.util.StacktraceFormatter;
import lombok.Getter;
import lombok.Setter;

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
    private T delegate;

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

}

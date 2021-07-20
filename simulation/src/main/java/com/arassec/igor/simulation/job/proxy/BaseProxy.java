package com.arassec.igor.simulation.job.proxy;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.job.IgorConnectorUtil;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.util.StacktraceFormatter;
import lombok.Getter;
import lombok.Setter;

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
     * Might contain an error cause if the proxied component failed abnormally.
     */
    private String errorCause;

    /**
     * The maximum number of data items that should be processed in a simulated job execution.
     */
    protected int simulationLimit;

    /**
     * Keeps track of the number of processed data items to limit them according to the simulation limit configuration.
     */
    protected int processed;

    /**
     * Creates a new instance.
     *
     * @param delegate The original {@link IgorComponent}.
     * @param simulationLimit The maximum number of data items that should be processed in a simulated job execution.
     */
    BaseProxy(T delegate, int simulationLimit) {
        this.delegate = delegate;
        this.simulationLimit = simulationLimit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(JobExecution jobExecution) {
        try {
            delegate.initialize(jobExecution);
            IgorConnectorUtil.initializeConnectors(delegate, jobExecution);
        } catch (Exception e) {
            setErrorCause(StacktraceFormatter.format(e));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown(JobExecution jobExecution) {
        try {
            delegate.shutdown(jobExecution);
            IgorConnectorUtil.shutdownConnectors(delegate, jobExecution);
        } catch (Exception e) {
            errorCause = StacktraceFormatter.format(e);
        }
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

package com.arassec.igor.web.simulation;

import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.util.StacktraceFormatter;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Proxy for {@link Provider}s, that collects the provided data for later processing.
 */
@Getter
@Setter
public class ProviderProxy extends BaseProxy<Provider> implements Provider {

    /**
     * Contains the number of data items that were actually returned by this proxy so far.
     */
    private int curItemsProcessed = 0;

    /**
     * The collected data.
     */
    private List<Map<String, Object>> collectedData = new LinkedList<>();

    /**
     * Creates a new proxy instance.
     *
     * @param delegate The real provider proxied by this instance.
     */
    public ProviderProxy(Provider delegate) {
        super(delegate);
    }

    /**
     * Returns {@code true}, if the {@link #getSimulationLimit()} simulationLimit} has not been hit and the proxied provider has
     * more data to provide.
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
            setErrorCause(StacktraceFormatter.format(e));
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
            setErrorCause(StacktraceFormatter.format(e));
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        delegate.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSimulationLimit() {
        return delegate.getSimulationLimit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSimulationLimit(int limit) {
        delegate.setSimulationLimit(limit);
    }

}

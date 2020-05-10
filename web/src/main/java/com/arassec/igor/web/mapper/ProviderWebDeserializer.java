package com.arassec.igor.web.mapper;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.repository.ConnectorRepository;
import com.arassec.igor.web.simulation.ProviderProxy;

import java.util.Map;

/**
 * Desrializer for {@link Provider}s.
 */
public class ProviderWebDeserializer extends IgorComponentWebDeserializer<Provider> {
    /**
     * Creates a new deserializer instance.
     *
     * @param igorComponentRegistry The component registry.
     * @param connectorRepository   The connector repository to load connectors from.
     * @param simulationMode        Set to {@code true} if the resulting  components are used during simulated job runs.
     */
    public ProviderWebDeserializer(IgorComponentRegistry igorComponentRegistry, ConnectorRepository connectorRepository, boolean simulationMode) {
        super(Provider.class, igorComponentRegistry, connectorRepository, simulationMode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Provider createInstance(String typeId, Map<String, Object> parameters, boolean simulationMode) {
        Provider provider = igorComponentRegistry.createProviderInstance(typeId, parameters);
        if (simulationMode) {
            return new ProviderProxy(provider);
        } else {
            return provider;
        }
    }

}

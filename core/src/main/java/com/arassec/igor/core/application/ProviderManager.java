package com.arassec.igor.core.application;

import com.arassec.igor.core.application.factory.ProviderFactory;
import com.arassec.igor.core.model.provider.Provider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Manages {@link Provider}s. Entry point from outside the core package to create and maintain providers.
 */
@Slf4j
@Component
public class ProviderManager {

    /**
     * The factory for providers.
     */
    @Autowired
    private ProviderFactory providerFactory;

    /**
     * Returns all available provider types.
     *
     * @return Set of provider types.
     */
    public Set<String> getTypes() {
        return providerFactory.getTypes();
    }

    /**
     * Creates a provider of the given type with the given configuration.
     *
     * @param type       The provider's type.
     * @param parameters The property values to set in the provider instance.
     * @return A newly created {@link Provider} with the given configuration.
     */
    public Provider createProvider(String type, Map<String, Object> parameters) {
        return providerFactory.createInstance(type, parameters, false);
    }
}

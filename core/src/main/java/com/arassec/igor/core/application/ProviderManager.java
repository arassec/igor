package com.arassec.igor.core.application;

import com.arassec.igor.core.application.factory.ProviderFactory;
import com.arassec.igor.core.model.provider.Provider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Manages {@link Provider}s. Entry point from outside the core package to create and maintain providers.
 */
@Slf4j
@Component
public class ProviderManager extends ModelManager<Provider> {

    /**
     * Creates a new instance.
     *
     * @param providerFactory The factory to create {@link Provider}s.
     */
    public ProviderManager(ProviderFactory providerFactory) {
        super(providerFactory);
    }

}

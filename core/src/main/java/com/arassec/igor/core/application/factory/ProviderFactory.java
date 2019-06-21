package com.arassec.igor.core.application.factory;

import com.arassec.igor.core.model.IgorProvider;
import com.arassec.igor.core.model.IgorProviderCategory;
import com.arassec.igor.core.model.provider.Provider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory for Providers.
 */
@Slf4j
@Component
public class ProviderFactory extends ModelFactory<Provider> {

    /**
     * Creates a new {@link ProviderFactory}.
     */
    public ProviderFactory() {
        super(Provider.class, IgorProviderCategory.class, IgorProvider.class);
    }

}

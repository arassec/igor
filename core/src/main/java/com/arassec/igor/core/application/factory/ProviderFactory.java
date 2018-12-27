package com.arassec.igor.core.application.factory;

import com.arassec.igor.core.model.IgorProvider;
import com.arassec.igor.core.model.provider.Provider;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
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
        super(Provider.class, IgorProvider.class);
    }

}

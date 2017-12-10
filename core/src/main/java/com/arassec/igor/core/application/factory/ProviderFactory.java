package com.arassec.igor.core.application.factory;

import com.arassec.igor.core.model.IgorProvider;
import com.arassec.igor.core.model.provider.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

/**
 * Factory for Providers.
 */
@Component
public class ProviderFactory extends ModelFactory<Provider> {

    private static final Logger LOG = LoggerFactory.getLogger(ProviderFactory.class);

    public ProviderFactory() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(IgorProvider.class));
        // TODO: Get this from "Igorfile"...
        for (BeanDefinition beanDefinition : scanner.findCandidateComponents("com.arassec.igor")) {
            try {
                Class<?> c = Class.forName(beanDefinition.getBeanClassName());
                if (Provider.class.isAssignableFrom(c)) {
                    String className = beanDefinition.getBeanClassName();
                    typeToClass.put(Class.forName(className).getAnnotation(IgorProvider.class).type(), className);
                    LOG.debug("Registered provider: {}", beanDefinition.getBeanClassName());
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }

}

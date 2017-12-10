package com.arassec.igor.core.application.factory;

import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

/**
 * Factory for Services.
 */
@Component
public class ServiceFactory extends ModelFactory<Service> {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceFactory.class);

    public ServiceFactory() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(IgorService.class));
        // TODO: Get this from "Igorfile"...
        for (BeanDefinition beanDefinition : scanner.findCandidateComponents("com.arassec.igor")) {
            try {
                Class<?> c = Class.forName(beanDefinition.getBeanClassName());
                if (Service.class.isAssignableFrom(c)) {
                    String className = beanDefinition.getBeanClassName();
                    typeToClass.put(Class.forName(className).getAnnotation(IgorService.class).type(), className);
                    LOG.debug("Registered service: {}", beanDefinition.getBeanClassName());
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }

}

package com.arassec.igor.core.application.factory;

import com.arassec.igor.core.application.factory.util.ServiceCategoryComponentProvider;
import com.arassec.igor.core.application.schema.ServiceCategory;
import com.arassec.igor.core.application.schema.ServiceType;
import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.IgorServiceCategory;
import com.arassec.igor.core.model.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Factory for Services.
 */
@Component
public class ServiceFactory extends ModelFactory<Service> {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceFactory.class);

    private Set<ServiceCategory> serviceCategories = new HashSet<>();
    private Set<ServiceType> serviceTypes = new HashSet<>();
    private Map<String, Set<ServiceType>> typesByCategory = new HashMap<>();
    private Map<String, ServiceCategory> categoryByType = new HashMap<>();

    public ServiceFactory() {
        loadServiceCategories();
        loadServiceTypesByCategory();
    }

    public Set<ServiceCategory> getServiceCategories() {
        return serviceCategories;
    }

    public Set<ServiceType> getTypesByCategory(String category) {
        if (typesByCategory.containsKey(category)) {
            return typesByCategory.get(category);
        }
        return new HashSet<>();
    }

    public ServiceCategory getCategory(Service service) {
        ServiceType serviceType = getType(service);
        if (categoryByType.containsKey(serviceType.getType())) {
            return categoryByType.get(serviceType.getType());
        }
        return null;
    }

    public ServiceType getType(Service service) {
        String type = service.getClass().getName();
        List<ServiceType> collectedServiceTypes = serviceTypes.stream().filter(serviceType -> serviceType.getType().equals(type)).collect(Collectors.toList());
        if (collectedServiceTypes.size() > 0) {
            return collectedServiceTypes.get(0);
        }
        return null;
    }

    private void loadServiceCategories() {
        ClassPathScanningCandidateComponentProvider scanner = new ServiceCategoryComponentProvider();
        for (BeanDefinition beanDefinition : scanner.findCandidateComponents("com.arassec.igor")) {
            try {
                Class<?> igorServiceClass = Class.forName(beanDefinition.getBeanClassName());
                if (Service.class.isAssignableFrom(igorServiceClass)) {
                    serviceCategories.add(new ServiceCategory(igorServiceClass.getName(),
                            igorServiceClass.getAnnotation(IgorServiceCategory.class).label()));
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void loadServiceTypesByCategory() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(IgorService.class));
        // TODO: Get this from "Igorfile"...
        for (BeanDefinition beanDefinition : scanner.findCandidateComponents("com.arassec.igor")) {
            try {
                Class<?> igorService = Class.forName(beanDefinition.getBeanClassName());
                if (Service.class.isAssignableFrom(igorService)) {
                    String className = beanDefinition.getBeanClassName();
                    ServiceCategory serviceCategory = findServiceCategory(igorService);
                    ServiceType serviceType = new ServiceType(className, igorService.getAnnotation(IgorService.class).label());
                    if (!typesByCategory.containsKey(serviceCategory.getType())) {
                        typesByCategory.put(serviceCategory.getType(), new HashSet<>());
                    }
                    serviceCategories.add(serviceCategory);
                    serviceTypes.add(serviceType);
                    typesByCategory.get(serviceCategory.getType()).add(serviceType);
                    categoryByType.put(className, serviceCategory);
                    types.add(className);
                    LOG.debug("Registered service '{}' with category '{}'", beanDefinition.getBeanClassName(),
                            serviceCategory.getType());
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private ServiceCategory findServiceCategory(Class<?> igorService) {
        for (ServiceCategory serviceCategory : serviceCategories) {
            try {
                if (Class.forName(serviceCategory.getType()).isAssignableFrom(igorService)) {
                    return serviceCategory;
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalArgumentException("No service category for class '" + igorService.getName() + "' defined!");
    }

}

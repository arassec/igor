package com.arassec.igor.web.api.util;

import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.IgorServiceCategory;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.web.api.model.ServiceCategory;
import com.arassec.igor.web.api.model.ServiceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ServiceUtil implements InitializingBean {

    @Autowired
    private ServiceManager serviceManager;

    private Set<ServiceCategory> serviceCategories = new HashSet<>();
    private Set<ServiceType> serviceTypes = new HashSet<>();
    private Map<String, Set<ServiceType>> typesByCategory = new HashMap<>();
    private Map<String, ServiceCategory> categoryByType = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        loadServiceCategories();
        serviceManager.getTypes().stream().forEach(type -> {
            Service service = serviceManager.createService(type, null);
            ServiceCategory serviceCategory = findServiceCategory(service.getClass());
            ServiceType serviceType = new ServiceType(type, service.getClass().getAnnotation(IgorService.class).label());
            if (!typesByCategory.containsKey(serviceCategory.getType())) {
                typesByCategory.put(serviceCategory.getType(), new HashSet<>());
            }
            serviceTypes.add(serviceType);
            typesByCategory.get(serviceCategory.getType()).add(serviceType);
            categoryByType.put(type, serviceCategory);
        });
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
                    log.info("Registered service category: {}", igorServiceClass.getName());
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

package com.arassec.igor.web.api.util;

import com.arassec.igor.core.model.IgorServiceCategory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

public class ServiceCategoryComponentProvider extends ClassPathScanningCandidateComponentProvider {

    public ServiceCategoryComponentProvider() {
        super(false);
        addIncludeFilter(new AnnotationTypeFilter(IgorServiceCategory.class));
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface();
    }
}

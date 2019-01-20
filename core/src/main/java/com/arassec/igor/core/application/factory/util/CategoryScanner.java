package com.arassec.igor.core.application.factory.util;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

/**
 * Scanner for annotations that are set on an interface. Igor uses those to sort different classes into categories for
 * the UI.
 * <p>
 * For example, the {@link com.arassec.igor.core.model.IgorServiceCategory} annotation can be used on an interface to
 * sort all the implementations of that interface into that category.
 */
public class CategoryScanner extends ClassPathScanningCandidateComponentProvider {

    /**
     * Creates a new instance.
     *
     * @param annotationClass The annotation class to scan the classpath for.
     */
    public CategoryScanner(Class<? extends Annotation> annotationClass) {
        super(false);
        addIncludeFilter(new AnnotationTypeFilter(annotationClass));
    }

    /**
     * Checks whether a component is a candidate for a category. This returns {@code true} for interfaces only.
     *
     * @return {@code true}, if the bean is an interface, {@code false} otherwise.
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface();
    }

}

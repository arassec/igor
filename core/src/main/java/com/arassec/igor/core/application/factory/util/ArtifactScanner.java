package com.arassec.igor.core.application.factory.util;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.annotation.Annotation;

/**
 * Scanner for igor artifacts, be it category definitions or components.
 */
public class ArtifactScanner extends ClassPathScanningCandidateComponentProvider {

    /**
     * Indicates whether only interfaces are allowed as artifact or not.
     */
    private boolean requiresInterface;

    /**
     * Creates a new instance.
     *
     * @param modelClass The model's class, that the artifact has to be assignable to.
     * @param annotationClass The class of the annotation that must be present on valid artifacts.
     * @param requiresInterface Set to {@code true}, if only interfaces should be allowed as artifact.
     */
    public ArtifactScanner(Class<?> modelClass, Class<? extends Annotation> annotationClass, boolean requiresInterface) {
        super(false);
        this.requiresInterface = requiresInterface;
        AssignableTypeFilter modelClassFilter = new AssignableTypeFilter(modelClass);
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(annotationClass);
        addIncludeFilter((metadataReader, metadataReaderFactory) -> annotationTypeFilter.match(metadataReader, metadataReaderFactory) && modelClassFilter.match(metadataReader, metadataReaderFactory));
    }

    /**
     * Checks whether a component is a candidate for a category. This returns {@code true} for interfaces only.
     *
     * @return {@code true}, if the bean is an interface, {@code false} otherwise.
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        if (requiresInterface) {
            return beanDefinition.getMetadata().isInterface();
        } else {
            return !beanDefinition.getMetadata().isInterface();
        }
    }

}

package com.arassec.igor.core.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class as external service for data. All classes annotated as {@code IgorService} are fetched during
 * startup and can be created with the {@link com.arassec.igor.core.application.factory.ServiceFactory}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgorService {

    /**
     * The service's label for the UI.
     *
     * @return The label.
     */
    String label();

}

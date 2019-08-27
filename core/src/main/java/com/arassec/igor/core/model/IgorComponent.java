package com.arassec.igor.core.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class as component of the igor application. All classes annotated as {@code IgorComponent} are fetched during
 * startup and can be created with their respective {@link com.arassec.igor.core.application.factory.ModelFactory}, depending
 * on the interfaces they implement.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgorComponent {

    /**
     * The component's label for the UI.
     *
     * @return The label.
     */
    String value();

}

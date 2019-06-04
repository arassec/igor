package com.arassec.igor.core.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class as action of a job. Every class annotated as {@code IgorAction} can be instantiated with the
 * {@link com.arassec.igor.core.application.factory.ActionFactory} after startup.
 * <p>
 * An {@code IgorAction} is a single piece of work that is applied to data provided in a Task.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgorAction {

    /**
     * The action's label for the UI.
     *
     * @return The label.
     */
    String label();

}

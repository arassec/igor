package com.arassec.igor.core.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class as trigger of a job. Every class annotated as {@code IgorTrigger} can be instantiated with the
 * {@link com.arassec.igor.core.application.factory.TriggerFactory} after startup.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgorTrigger {

    /**
     * The trigger's label for the UI.
     *
     * @return The label.
     */
    String label();

}

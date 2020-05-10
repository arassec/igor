package com.arassec.igor.core.model.annotation;

import com.arassec.igor.core.model.connector.Connector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a {@link Connector} method as safe for execution during a simulated job execution.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgorSimulationSafe {
}

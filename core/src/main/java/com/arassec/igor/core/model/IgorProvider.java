package com.arassec.igor.core.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class as provider for data. All classes marked as {@code IgorProvider} can be created with the
 * {@link com.arassec.igor.core.application.factory.ProviderFactory} after startup.
 * <p>
 * A {@link IgorProvider} is used in the {@link com.arassec.igor.core.model.job.Task} configuration to provide the
 * initial set of data that the task operates on.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgorProvider {

    /**
     * The provider's label for the UI.
     *
     * @return The label.
     */
    String label();

}

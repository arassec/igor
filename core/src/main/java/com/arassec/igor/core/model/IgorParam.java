package com.arassec.igor.core.model;

import com.arassec.igor.core.model.misc.ParameterSubtype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a member variable of an {@link IgorService}, {@link IgorProvider} or {@link IgorAction} as configuration
 * parameter, which should be made configurable with the UI.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgorParam {

    /**
     * Indicates whether this parameter has to be encrypted before saving it to a repository. If set to {@code true},
     * the parameter is masked in the UI by default, and encrypted in the repository.
     *
     * @return {@code true}, if this parameter should be secured, {@code false} otherwise.
     */
    boolean secured() default false;

    /**
     * Indicates whether a parameter is optional and must not be set in the UI by the user.
     *
     * @return {@code true}, if the parameter is optional and not needed for providing the service of the containing
     * class. {@code false}, if the parameter must be set.
     */
    boolean optional() default false;

    /**
     * Further specifies the type of parameter, additionally to the java data type.
     *
     * @return A value of {@link ParameterSubtype}.
     */
    ParameterSubtype subtype() default ParameterSubtype.NONE;

}

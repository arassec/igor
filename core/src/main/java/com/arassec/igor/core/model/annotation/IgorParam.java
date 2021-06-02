package com.arassec.igor.core.model.annotation;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a member variable of an {@link IgorComponent} as configuration parameter.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgorParam {

    /**
     * The sort index of this parameter for the UI.
     *
     * @return The sort index of this parameter.
     */
    int sortIndex() default 0;

    /**
     * Indicates whether this parameter has to be encrypted before saving it to a repository. If set to {@code true}, the
     * parameter is masked in the UI by default, and encrypted in the repository.
     *
     * @return {@code true}, if this parameter should be secured, {@code false} otherwise.
     */
    boolean secured() default false;

    /**
     * Indicates whether a parameter should be displayed in the advanced section of the parameter configuration UI.
     *
     * @return {@code true}, if the parameter is advanced and can be hidden by default in the UI. {@code false}, if the parameter
     * must always be shown to the user in the UI.
     */
    boolean advanced() default false;

    /**
     * Further specifies the type of parameter, additionally to the java data type.
     *
     * @return A value of {@link ParameterSubtype}.
     */
    ParameterSubtype subtype() default ParameterSubtype.NONE;

}

package com.arassec.igor.core.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a member variable as configuration parameter.
 * <p>
 * Created by Andreas Sensen on 01.05.2017.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgorParam {

    boolean secured() default false;

    boolean optional() default false;

}

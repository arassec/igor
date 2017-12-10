package com.arassec.igor.core.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class as provider for data.
 * <p>
 * Created by Andreas Sensen on 01.05.2017.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgorProvider {

    /**
     * Defines the type of the provider.
     *
     * @return The provider's type.
     */
    String type();

}

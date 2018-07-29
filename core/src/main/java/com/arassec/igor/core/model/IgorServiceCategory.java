package com.arassec.igor.core.model;

import java.lang.annotation.*;

/**
 * Annotates a class as service category.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgorServiceCategory {

    String label();

}

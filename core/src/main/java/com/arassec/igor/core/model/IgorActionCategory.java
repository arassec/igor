package com.arassec.igor.core.model;


import java.lang.annotation.*;

/**
 * Annotates a class as action category. This has no other use case than to sort actions into different categories
 * in the UI.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgorActionCategory {

    /**
     * The categorie's label for the UI.
     *
     * @return The label.
     */
    String label();

}

package com.arassec.igor.core.model;

import java.lang.annotation.*;

/**
 * Annotates a class as service category. This has no other use case than to sort services into different categories in
 * the UI.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgorServiceCategory {

    /**
     * The categorie's label for the UI.
     *
     * @return The label.
     */
    String label();

}

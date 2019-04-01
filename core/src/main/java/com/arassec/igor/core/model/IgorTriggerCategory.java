package com.arassec.igor.core.model;

import java.lang.annotation.*;

/**
 * Annotates a class as trigger category. This has no other use case than to sort triggers into different categories in
 * the UI.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgorTriggerCategory {

    /**
     * The categorie's label for the UI.
     *
     * @return The label.
     */
    String label();

}

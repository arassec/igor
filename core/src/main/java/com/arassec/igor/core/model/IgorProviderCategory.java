package com.arassec.igor.core.model;

import java.lang.annotation.*;

/**
 * Annotates a class as provider category. This has no other use case than to sort providers into different categories
 * in the UI.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgorProviderCategory {

    /**
     * The categorie's label for the UI.
     *
     * @return The label.
     */
    String label();

}
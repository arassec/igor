package com.arassec.igor.core.model;

/**
 * Defines a component in the igor application.
 */
public interface IgorComponent {

    /**
     * Returns the ID of the category of this component.
     *
     * @return The unique category ID.
     */
    String getCategoryId();

    /**
     * Returns the ID of the type of this component.
     *
     * @return The unique type ID.
     */
    String getTypeId();

}

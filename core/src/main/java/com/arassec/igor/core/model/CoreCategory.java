package com.arassec.igor.core.model;

import lombok.Getter;

/**
 * Defines categories for core components.
 */
public enum CoreCategory {

    /**
     * The "Core" category.
     */
    CORE("core");

    /**
     * The category's ID.
     */
    @Getter
    private final String id;

    /**
     * Creates a new category ID.
     *
     * @param id The ID to use.
     */
    CoreCategory(String id) {
        this.id = id;
    }

}

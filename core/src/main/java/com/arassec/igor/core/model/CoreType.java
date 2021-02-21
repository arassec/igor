package com.arassec.igor.core.model;

import lombok.Getter;

/**
 * Defines types for core components.
 */
public enum CoreType {

    /**
     * The "Missing Trigger" type.
     */
    MISSING_COMPONENT_TRIGGER("missing-component-trigger"),

    /**
     * The "Missing Action" type.
     */
    MISSING_COMPONENT_ACTION("missing-component-action"),

    /**
     * The "Missing Connector" type.
     */
    MISSING_COMPONENT_CONNECTOR("missing-component-connector");

    /**
     * The type's ID.
     */
    @Getter
    private final String id;

    /**
     * Creates a new type ID.
     *
     * @param id The ID to use.
     */
    CoreType(String id) {
        this.id = id;
    }

}

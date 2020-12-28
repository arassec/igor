package com.arassec.igor.plugin.core;

import lombok.Getter;

/**
 * Defines categories for common components.
 */
public enum CoreCategory {

    /**
     * The "Util" category.
     */
    UTIL("util"),

    /**
     * The "Web" category.
     */
    WEB("web"),

    /**
     * The "File" category.
     */
    FILE("file"),

    /**
     * The "Message" category.
     */
    MESSAGE("message"),

    /**
     * The "Persistence" category.
     */
    PERSISTENCE("persistence");

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

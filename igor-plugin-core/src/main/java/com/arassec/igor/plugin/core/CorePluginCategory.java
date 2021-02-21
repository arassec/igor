package com.arassec.igor.plugin.core;

import lombok.Getter;

/**
 * Defines categories for components of the 'core' plugin.
 */
public enum CorePluginCategory {

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
     * The "Test" category.
     */
    TEST("test"),

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
    CorePluginCategory(String id) {
        this.id = id;
    }

}

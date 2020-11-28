package com.arassec.igor.plugin.common;

import lombok.Getter;

/**
 * Defines categories for common components.
 */
public enum CommonCategory {

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
    CommonCategory(String id) {
        this.id = id;
    }

}

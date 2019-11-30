package com.arassec.igor.persistence.mapper;

import lombok.Getter;

/**
 * Contains the keys for the persistence mapper.
 */
public enum PersistenceMapperKey {

    /**
     * The ID key.
     */
    ID("id"),

    /**
     * The name key.
     */
    NAME("name"),

    /**
     * The active key.
     */
    ACTIVE("active"),

    /**
     * The parameters key.
     */
    PARAMETERS("parameters"),

    /**
     * The service key.
     */
    SERVICE("service"),

    /**
     * The value key.
     */
    VALUE("value"),

    /**
     * The secured key.
     */
    SECURED("secured"),

    /**
     * The type key.
     */
    TYPE_ID("typeId");

    /**
     * The key as String.
     */
    @Getter
    private String key;

    /**
     * Creates a new instance.
     *
     * @param key The key as String.
     */
    PersistenceMapperKey(String key) {
        this.key = key;
    }

}

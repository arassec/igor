package com.arassec.igor.web.mapper;

import lombok.Getter;

/**
 * Contains the keys for the web mapper.
 */
public enum WebMapperKey {

    /**
     * The ID key.
     */
    ID("id"),

    /**
     * The type key.
     */
    TYPE("type"),

    /**
     * The name key.
     */
    NAME("name"),

    /**
     * The key key. :)
     */
    KEY("key"),

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
     * The category key.
     */
    CATEGORY("category"),

    /**
     * The secured key.
     */
    SECURED("secured"),

    /**
     * The optional key.
     */
    OPTIONAL("optional"),

    /**
     * The configurable key.
     */
    CONFIGURABLE("configurable"),

    /**
     * The subtype key.
     */
    SUBTYPE("subtype"),

    /**
     * The service-name key.
     */
    SERVICE_NAME("serviceName"),

    /**
     * The service-class key.
     */
    SERVICE_CLASS("serviceClass");

    /**
     * Contains the key as String.
     */
    @Getter
    private String key;

    /**
     * Creates a new instance.
     *
     * @param key The key.
     */
    WebMapperKey(String key) {
        this.key = key;
    }

}

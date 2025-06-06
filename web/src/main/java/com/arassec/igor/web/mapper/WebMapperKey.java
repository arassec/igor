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
     * The 'typeCandidates' key.
     */
    TYPE_CANDIDATES("typeCandidates"),

    /**
     * The name key.
     */
    NAME("name"),

    /**
     * The 'displayName' key.
     */
    DISPLAY_NAME("displayName"),

    /**
     * The description key.
     */
    DESCRIPTION("description"),

    /**
     * The 'key' key. :)
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
     * The connector key.
     */
    CONNECTOR("connector"),

    /**
     * The value key.
     */
    VALUE("value"),

    /**
     * The category key.
     */
    CATEGORY("category"),

    /**
     * The 'categoryCandidates' key.
     */
    CATEGORY_CANDIDATES("categoryCandidates"),

    /**
     * The secured key.
     */
    SECURED("secured"),

    /**
     * The optional key.
     */
    ADVANCED("advanced"),

    /**
     * The required key.
     */
    REQUIRED("required"),

    /**
     * The configurable key.
     */
    CONFIGURABLE("configurable"),

    /**
     * The subtype key.
     */
    SUBTYPE("subtype"),

    /**
     * The connector-name key.
     */
    CONNECTOR_NAME("connectorName"),

    /**
     * The connector-class key.
     */
    CONNECTOR_CLASS("connectorClass"),

    /**
     * Indicates whether documentation is available or not.
     */
    DOCUMENTATION_AVAILABLE("documentationAvailable"),

    /**
     * Contains general validation errors.
     */
    GENERAL_VALIDATION_ERRORS("generalValidationErrors"),

    /**
     * Contains general errors.
     */
    GENERAL_ERROR("generalError"),

    /**
     * Indicates whether a component supports events or not.
     */
    SUPPORTS_EVENTS("supportsEvents");

    /**
     * Contains the key as String.
     */
    @Getter
    private final String key;

    /**
     * Creates a new instance.
     *
     * @param key The key.
     */
    WebMapperKey(String key) {
        this.key = key;
    }

}

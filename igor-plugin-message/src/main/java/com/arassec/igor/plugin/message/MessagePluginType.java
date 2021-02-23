package com.arassec.igor.plugin.message;

import lombok.Getter;

/**
 * Defines types for components of the 'message' plugin.
 */
public enum MessagePluginType {

    /**
     * Type of the "RabbitMQ Message" connector.
     */
    RABBITMQ_MESSAGE_CONNECTOR("rabbitmq-message-connector");

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
    MessagePluginType(String id) {
        this.id = id;
    }

}

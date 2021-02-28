package com.arassec.igor.plugin.message;

import lombok.Getter;

/**
 * Defines types for components of the 'message' plugin.
 */
public enum MessagePluginType {

    /**
     * Type of the "RabbitMQ Send RabbitMqMessage" action.
     */
    RABBITMQ_SEND_MESSAGE_ACTION("rabbitmq-send-message-action"),

    /**
     * Type of the "RabbitMQ RabbitMqMessage" trigger.
     */
    RABBITMQ_MESSAGE_TRIGGER("rabbitmq-message-trigger"),

    /**
     * Type of the "RabbitMQ RabbitMqMessage" connector.
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

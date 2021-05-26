package com.arassec.igor.plugin.message;

import lombok.Getter;

/**
 * Defines types for components of the 'message' plugin.
 */
public enum MessagePluginType {

    /**
     * Type of the "RabbitMQ Send Message" action.
     */
    SEND_RABBITMQ_MESSAGE_ACTION("send-rabbitmq-message-action"),

    /**
     * Type of the "RabbitMQ Message" trigger.
     */
    RABBITMQ_MESSAGE_TRIGGER("rabbitmq-message-trigger"),

    /**
     * Type of the "RabbitMQ Message" connector.
     */
    RABBITMQ_MESSAGE_CONNECTOR("rabbitmq-message-connector"),

    /**
     * Type of the "Send E-Mail" action.
     */
    SEND_EMAIL_MESSAGE_ACTION("send-email-message-action"),

    /**
     * Type of the "Receive E-Mail" action.
     */
    RECEIVE_EMAIL_MESSAGE_ACTION("receive-email-message-action"),

    /**
     * Type of the "E-Mail SMTP" message connector.
     */
    EMAIL_SMTP_MESSAGE_CONNECTOR("email-smtp-message-connector"),

    /**
     * Type of the "E-Mail IMAP" message connector.
     */
    EMAIL_IMAP_MESSAGE_CONNECTOR("email-imap-message-connector");

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

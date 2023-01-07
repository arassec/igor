package com.arassec.igor.plugin.message;

/**
 * Defines types for components of the 'message' plugin.
 */
public final class MessageType {

    /**
     * Type of the "RabbitMQ Send Message" action.
     */
    public static final String SEND_RABBITMQ_ACTION = "send-rabbitmq-message-action";

    /**
     * Type of the "RabbitMQ Message" trigger.
     */
    public static final String RABBITMQ_TRIGGER = "rabbitmq-message-trigger";

    /**
     * Type of the "RabbitMQ Message" connector.
     */
    public static final String RABBITMQ_CONNECTOR = "rabbitmq-message-connector";

    /**
     * Type of the "Send E-Mail" action.
     */
    public static final String SEND_EMAIL_ACTION = "send-email-message-action";

    /**
     * Type of the "Receive E-Mail" action.
     */
    public static final String RECEIVE_EMAIL_ACTION = "receive-email-message-action";

    /**
     * Type of the "E-Mail SMTP" message connector.
     */
    public static final String EMAIL_SMTP_CONNECTOR = "email-smtp-message-connector";

    /**
     * Type of the "E-Mail IMAP" message connector.
     */
    public static final String EMAIL_IMAP_CONNECTOR = "email-imap-message-connector";

    /**
     * Creates a new type ID.
     */
    private MessageType() {
        // Prevent instantiation...
    }

}

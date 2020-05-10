package com.arassec.igor.module.message.connector;

/**
 * Can be used as fallback for any {@link javax.validation.constraints.NotNull} connector parameter.
 */
public class FallbackMessageConnector extends BaseMessageConnector {

    /**
     * The exception message to use.
     */
    private static final String ERROR_MESSAGE = "Configure a real message connector!";

    /**
     * Creates a new component instance.
     */
    public FallbackMessageConnector() {
        super("fallback-message-connector");
    }

    /**
     * Throws an {@link IllegalStateException} on every invocation!
     */
    @Override
    public void sendMessage(Message message) {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

    /**
     * Throws an {@link IllegalStateException} on every invocation!
     */
    @Override
    public void testConfiguration() {
        throw new IllegalStateException(ERROR_MESSAGE);
    }

}

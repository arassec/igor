package com.arassec.igor.module.message.service;

/**
 * Can be used as fallback for any {@link javax.validation.constraints.NotNull} service parameter.
 */
public class FallbackMessageService extends BaseMessageService {

    /**
     * The exception message to use.
     */
    private static final String ERROR_MESSAGE = "Configure a real message service!";

    /**
     * Creates a new component instance.
     */
    public FallbackMessageService() {
        super("fallback-message-service");
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

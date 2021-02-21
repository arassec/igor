package com.arassec.igor.plugin.core.message.connector;

import com.arassec.igor.plugin.core.CorePluginType;

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
        super(CorePluginType.FALLBACK_MESSAGE_CONNECTOR.getId());
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
    public void enableMessageRetrieval() {
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

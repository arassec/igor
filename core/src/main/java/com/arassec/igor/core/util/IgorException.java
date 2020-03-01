package com.arassec.igor.core.util;

/**
 * Exception for all errors that occur during a service interaction.
 */
public class IgorException extends RuntimeException {

    /**
     * Creates a new {@link IgorException}.
     *
     * @param message The message to use.
     */
    public IgorException(String message) {
        super(message);
    }

    /**
     * Creates a new {@link IgorException}.
     *
     * @param message The message to use.
     * @param cause   The underlying cause of the error.
     */
    public IgorException(String message, Throwable cause) {
        super(message, cause);
    }

}

package com.arassec.igor.core.model.service;

/**
 * Exception for all errors that occur during a service interaction.
 */
public class ServiceException extends RuntimeException {

    /**
     * Creates a new {@link ServiceException}.
     *
     * @param message The message to use.
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * Creates a new {@link ServiceException}.
     *
     * @param message The message to use.
     * @param cause   The underlying cause of the error.
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}

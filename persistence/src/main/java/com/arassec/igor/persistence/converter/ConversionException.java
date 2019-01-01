package com.arassec.igor.persistence.converter;

/**
 * Indicates a problem during model-conversion.
 */
public class ConversionException extends Exception {

    /**
     * Creates a new ConversionException.
     *
     * @param message A message indicating the problem.
     */
    public ConversionException(String message) {
        super(message);
    }

}

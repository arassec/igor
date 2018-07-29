package com.arassec.igor.persistence.converter;

/**
 * Indicates a problem during model-conversion.
 */
public class ConversionException extends Exception {

    public ConversionException(String message) {
        super(message);
    }
}

package com.arassec.igor.core.application.factory;

import lombok.Data;

/**
 * Defines a type with its corresponding label.
 */
@Data
public class ModelDefinition {

    /**
     * The type.
     */
    private String type;

    /**
     * The label.
     */
    private String label;

}

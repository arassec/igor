package com.arassec.igor.core.application.factory.util;

import lombok.Data;

/**
 * Defines a key with its corresponding label. Used e.g. in categories and types in the UI.
 */
@Data
public class KeyLabelStore {

    /**
     * The key.
     */
    private String key;

    /**
     * The label.
     */
    private String label;

}

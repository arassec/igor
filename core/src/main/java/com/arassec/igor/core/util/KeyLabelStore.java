package com.arassec.igor.core.util;

/**
 * Defines a key with its corresponding label. Used e.g. in categories and types in the UI.
 */
public class KeyLabelStore extends Pair<String, String> {

    public KeyLabelStore(String key, String value) {
        super(key, value);
    }
}

package com.arassec.igor.web.model;

import com.arassec.igor.core.util.Pair;
import lombok.NoArgsConstructor;

/**
 * Defines a key with its corresponding label. Used e.g. in categories and types in the UI.
 */
@NoArgsConstructor
public class KeyLabelStore extends Pair<String, String> {

    /**
     * Creates a new instance with values.
     *
     * @param key   The key.
     * @param value The label.
     */
    public KeyLabelStore(String key, String value) {
        super(key, value);
    }

}

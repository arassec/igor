package com.arassec.igor.web.model;

import lombok.Data;

/**
 * Contains information about an igor component type.
 */
@Data
public class TypeData extends KeyLabelStore {

    /**
     * {@code true}, if documentation for this component type is available, {@code false} otherwise.
     */
    private boolean documentationAvailable;

    /**
     * Creates a new TypeData instance.
     *
     * @param key                    The type's ID.
     * @param value                  The type's label.
     * @param documentationAvailable Set to {@code true} if documentation is available, {@code false} otherwise.
     */
    public TypeData(String key, String value, boolean documentationAvailable) {
        super(key, value);
        this.documentationAvailable = documentationAvailable;
    }

}

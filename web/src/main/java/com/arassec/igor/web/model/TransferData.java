package com.arassec.igor.web.model;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Models igor data for transfer (e.g. import/export).
 * <p>
 * Data is transferred as Map of Strings to Objects to avoid dependency conflicts (i.e. jobs requiring connectors which are not
 * available during deserialization).
 */
@Data
public class TransferData {

    /**
     * Jobs as JSON-Objects.
     */
    private List<Map<String, Object>> jobJsons = new LinkedList<>();

    /**
     * Connectors as JSON-Objects.
     */
    private List<Map<String, Object>> connectorJsons = new LinkedList<>();

}

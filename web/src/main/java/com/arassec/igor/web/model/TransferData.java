package com.arassec.igor.web.model;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * Models igor data for transfer (e.g. import/export).
 * <p>
 * Data is transferred as String to avoid dependency conflicts (i.e. jobs requiring connectors which are not available during
 * deserialization).
 */
@Data
public class TransferData {

    /**
     * Jobs as JSON-Strings.
     */
    private List<String> jobJsons = new LinkedList<>();

    /**
     * Connectors as JSON-Strings.
     */
    private List<String> connectorJsons = new LinkedList<>();

}
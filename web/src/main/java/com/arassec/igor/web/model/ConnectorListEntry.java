package com.arassec.igor.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains all data to display a connector in the connector overview list.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorListEntry {

    /**
     * The connector's ID.
     */
    private String id;

    /**
     * The name.
     */
    private String name;

    /**
     * Defines whether the connector is used ({@code true}) or not ({@code false}).
     */
    private boolean used;

}

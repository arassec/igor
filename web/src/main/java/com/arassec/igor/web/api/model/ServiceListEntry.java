package com.arassec.igor.web.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Contains all data to display a service in the service overview list.
 */
@Data
@AllArgsConstructor
public class ServiceListEntry {

    /**
     * The service's ID.
     */
    private Long id;

    /**
     * The name.
     */
    private String name;

    /**
     * Defines whether the service is used ({@code true}) or not ({@code false}).
     */
    private boolean used;

}

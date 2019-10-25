package com.arassec.igor.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Contains all data to display a job as list entry in the job overview.
 */
@Data
@AllArgsConstructor
public class JobListEntry {

    /**
     * The job's ID.
     */
    private String id;

    /**
     * The name.
     */
    private String name;

    /**
     * {@code true} if the job is active, {@code false} otherwise.
     */
    private boolean active;

}

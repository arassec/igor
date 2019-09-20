package com.arassec.igor.core.model.job.misc;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Models a persistent value from a job execution.
 */
@Data
@NoArgsConstructor
public class PersistentValue {

    /**
     * The value's ID.
     */
    private Long id;

    /**
     * The creation time of this value.
     */
    private Instant created;

    /**
     * The content of this value.
     */
    private String content;

    /**
     * Creates a new instance with the supplied content.
     *
     * @param content The content of this value.
     */
    public PersistentValue(String content) {
        this.content = content;
    }

}

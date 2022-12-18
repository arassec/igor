package com.arassec.igor.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

/**
 * JPA Entity for {@link com.arassec.igor.core.model.job.Job}s.
 */
@Data
@Entity
@Table(name = "job")
public class JobEntity {

    /**
     * The job's ID.
     */
    @Id
    private String id;

    /**
     * JPA version.
     */
    @Version
    private Integer version;

    /**
     * The job's name.
     */
    private String name;

    /**
     * The job as JSON-string.
     */
    private String content;

}

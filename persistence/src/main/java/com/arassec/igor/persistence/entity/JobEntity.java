package com.arassec.igor.persistence.entity;

import lombok.Data;

import javax.persistence.*;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

package com.arassec.igor.persistence.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * JPA Entity for {@link com.arassec.igor.core.model.job.Job}s.
 */
@Entity
@Table(name = "job", schema = "igor")
@Data
public class JobEntity {

    /**
     * The job's ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "jobIdSequence")
    @SequenceGenerator(name = "jobIdSequence", sequenceName = "JOB_ID_SEQUENCE")
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

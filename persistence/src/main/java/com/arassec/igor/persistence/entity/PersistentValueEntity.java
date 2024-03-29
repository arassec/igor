package com.arassec.igor.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/**
 * JPA entity for persistent values.
 */
@Data
@Entity
@Table(name = "persistent_value")
public class PersistentValueEntity {

    /**
     * The connector's ID.
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
     * The job's ID.
     */
    @Column(name = "job_id")
    private String jobId;

    /**
     * Date of creation of this value.
     */
    private Instant created;

    /**
     * The stored value.
     */
    private String content;

}

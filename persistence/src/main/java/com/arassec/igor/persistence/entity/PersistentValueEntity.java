package com.arassec.igor.persistence.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;

/**
 * JPA entity for TODO
 */
@Data
@Entity
@Table(name = "persistent_value", schema = "igor")
@SequenceGenerator(name = "persistentValueIdSequence", sequenceName = "PERSISTENT_VALUE_ID_SEQUENCE", allocationSize=1)
public class PersistentValueEntity {

    /**
     * The service's ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "persistentValueIdSequence")
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
    private Long jobId;

    /**
     * The task's ID.
     */
    private String taskId;

    /**
     * Date of creation of this value.
     */
    private Instant created;

    /**
     * The stored value.
     */
    private String content;

}

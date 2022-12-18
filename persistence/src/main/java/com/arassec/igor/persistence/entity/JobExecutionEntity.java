package com.arassec.igor.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * JPA Entity for {@link com.arassec.igor.core.model.job.execution.JobExecution}s.
 */
@Data
@Entity
@Table(name = "job_execution")
public class JobExecutionEntity {

    /**
     * The job-execution's ID.
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
     * The job-execution's state.
     */
    private String state;

    /**
     * The job-execution as JSON-string.
     */
    private String content;

}

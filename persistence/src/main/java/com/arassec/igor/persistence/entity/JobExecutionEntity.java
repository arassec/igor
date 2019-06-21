package com.arassec.igor.persistence.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * JPA Entity for {@link com.arassec.igor.core.model.job.execution.JobExecution}s.
 */
@Data
@Entity
@Table(name = "job_execution", schema = "igor")
@SequenceGenerator(name = "jobExecutionIdSequence", sequenceName = "JOB_EXECUTION_ID_SEQUENCE", allocationSize = 1)
public class JobExecutionEntity {

    /**
     * The job-execution's ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "jobExecutionIdSequence")
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
     * The job-execution's state.
     */
    private String state;

    /**
     * The job-execution as JSON-string.
     */
    private String content;

}

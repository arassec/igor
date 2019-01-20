package com.arassec.igor.persistence.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * JPA entity for {@link com.arassec.igor.core.model.service.Service}s.
 */
@Data
@Entity
@Table(name = "service", schema = "igor")
@SequenceGenerator(name = "serviceIdSequence", sequenceName = "SERVICE_ID_SEQUENCE", allocationSize=1)
public class ServiceEntity {

    /**
     * The service's ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "serviceIdSequence")
    private Long id;

    /**
     * JPA version.
     */
    @Version
    private Integer version;

    /**
     * The service's name.
     */
    private String name;

    /**
     * The service as JSON-string.
     */
    private String content;

}


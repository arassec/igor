package com.arassec.igor.persistence.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * JPA entity for {@link com.arassec.igor.core.model.service.Service}s.
 */
@Entity
@Table(name = "service", schema = "igor")
@Data
public class ServiceEntity {

    /**
     * The service's ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "serviceIdSequence")
    @SequenceGenerator(name = "serviceIdSequence", sequenceName = "SERVICE_ID_SEQUENCE")
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

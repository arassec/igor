package com.arassec.igor.persistence.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * JPA entity for {@link com.arassec.igor.core.model.service.Service}s.
 */
@Data
@Entity
@Table(name = "service")
public class ServiceEntity {

    /**
     * The service's ID.
     */
    @Id
    private String id;

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


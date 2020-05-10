package com.arassec.igor.persistence.entity;

import com.arassec.igor.core.model.connector.Connector;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * JPA entity for {@link Connector}s.
 */
@Data
@Entity
@Table(name = "connector")
public class ConnectorEntity {

    /**
     * The connector's ID.
     */
    @Id
    private String id;

    /**
     * JPA version.
     */
    @Version
    private Integer version;

    /**
     * The connector's name.
     */
    private String name;

    /**
     * The connector as JSON-string.
     */
    private String content;

}


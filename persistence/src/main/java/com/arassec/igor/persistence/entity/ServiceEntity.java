package com.arassec.igor.persistence.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by Andreas Sensen on 01.05.2017.
 */
@Entity
@Table(name = "service", schema = "igor")
@Data
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="serviceIdSequence")
    @SequenceGenerator(name="serviceIdSequence", sequenceName="SERVICE_ID_SEQUENCE")
    private Long id;

    @Version
    private Integer version;

    private String name;

    private String content;

}

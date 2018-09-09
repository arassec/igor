package com.arassec.igor.persistence.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by Andreas Sensen on 15.04.2017.
 */
@Entity
@Table(name = "job", schema = "igor")
@Data
public class JobEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="jobIdSequence")
    @SequenceGenerator(name="jobIdSequence", sequenceName="JOB_ID_SEQUENCE")
    private Long id;

    @Version
    private Integer version;

    private String name;

    private String content;

}

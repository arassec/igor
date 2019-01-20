package com.arassec.igor.core.application.converter.model;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.IgorService;
import lombok.Data;

/**
 * A dummy service for Unit-Tests.
 */
@Data
@IgorService(label = "Dummy service for unit tests")
public class DummyService {

    /**
     * int parameter.
     */
    @IgorParam
    private int intParam;

    /**
     * Integer parameter.
     */
    @IgorParam
    private Integer intObjectParam;

    /**
     * long parameter.
     */
    @IgorParam
    private long longParam;

    /**
     * Long parameter.
     */
    @IgorParam
    private Long longObjectParam;

    /**
     * boolean parameter.
     */
    @IgorParam
    private boolean booleanParam;

    /**
     * Boolean parameter.
     */
    @IgorParam
    private Boolean booleanObjectParam;

    /**
     * String parameter.
     */
    @IgorParam
    private String stringParam;

    /**
     * Encrypted string parameter.
     */
    @IgorParam(secured = true)
    private String securedParam;

    /**
     * Optional parameter.
     */
    @IgorParam(optional = true)
    private String optionalParam;

}

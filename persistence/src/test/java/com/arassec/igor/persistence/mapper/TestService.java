package com.arassec.igor.persistence.mapper;

import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.service.BaseService;
import com.arassec.igor.core.model.service.Service;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Service for testing the persistence mappers.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class TestService extends BaseService implements Service {

    /**
     * The service's category ID.
     */
    public static final String CATEGORY_ID = "service-category-id";

    /**
     * The service's type ID.
     */
    public static final String TYPE_ID = "service-type-id";

    /**
     * An example ID for the test service.
     */
    public static final String SERVICE_ID = "service-id";

    /**
     * Primitive int parameter.
     */
    @IgorParam
    private int intParam;

    /**
     * Integer object parameter.
     */
    @IgorParam
    private Integer integerParam;

    /**
     * Primitive long parameter.
     */
    @IgorParam
    private long longParam;

    /**
     * Long object parameter.
     */
    @IgorParam
    private Long longObjectParam;

    /**
     * Primitive boolean parameter.
     */
    @IgorParam
    private boolean booleanParam;

    /**
     * Boolean object parameter.
     */
    @IgorParam
    private Boolean booleanObjectParam;

    /**
     * String parameter.
     */
    @IgorParam
    private String stringParam;

    /**
     * Secured string parameter.
     */
    @IgorParam(secured = true)
    private String securedStringParam;

    /**
     * Creates a new component instance.
     */
    public TestService() {
        super(CATEGORY_ID, TYPE_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() {
        // not under test...
    }

}

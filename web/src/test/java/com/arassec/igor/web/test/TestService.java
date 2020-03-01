package com.arassec.igor.web.test;

import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.service.BaseService;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Service for testing the web layer.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class TestService extends BaseService implements TestServiceInterface {

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
     * Property to support testing if the testConfiguration method has been invoked.
     */
    private boolean testConfigurationInvoked = false;

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
     * Throws an {@link IllegalStateException} to test proxying this service.
     */
    @Override
    public void testConfiguration() {
        testConfigurationInvoked = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer simulationSafeMethod() {
        return 666;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String simulationUnsafeMethod() {
        return "this-should-not-be-returned-in-simulations";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @IgorSimulationSafe
    public String directlyAnnotatedSimulationSafeMethod() {
        return "real-value-from-service";
    }

}

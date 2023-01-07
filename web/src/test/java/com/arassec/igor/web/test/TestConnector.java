package com.arassec.igor.web.test;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.connector.BaseConnector;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Connector for testing the web layer.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@IgorComponent(categoryId = "connector-category-id", typeId = "connector-type-id")
public class TestConnector extends BaseConnector implements TestConnectorInterface {

    /**
     * An example ID for the test connector.
     */
    public static final String CONNECTOR_ID = "connector-id";

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
     * Throws an {@link IllegalStateException} to test proxying this connector.
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
        return "real-value-from-connector";
    }

}

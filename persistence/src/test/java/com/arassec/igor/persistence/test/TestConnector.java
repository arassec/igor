package com.arassec.igor.persistence.test;

import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.connector.BaseConnector;
import com.arassec.igor.core.model.connector.Connector;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Connector for testing the persistence layer.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class TestConnector extends BaseConnector implements Connector {

    /**
     * The connector's category ID.
     */
    public static final String CATEGORY_ID = "connector-category-id";

    /**
     * The connector's type ID.
     */
    public static final String TYPE_ID = "connector-type-id";

    /**
     * An example ID for the test connector.
     */
    public static final String CONNECTOR_ID = "connector-id";

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
    public TestConnector() {
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

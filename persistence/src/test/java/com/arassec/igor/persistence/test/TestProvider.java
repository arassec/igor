package com.arassec.igor.persistence.test;

import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.provider.BaseProvider;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * A provider for testing the persistence layer.
 */
public class TestProvider extends BaseProvider {

    /**
     * The provider's category.
     */
    public static final String CATEGORY_ID = "provider-category-id";

    /**
     * The provider's type.
     */
    public static final String TYPE_ID = "provider-type-id";

    /**
     * A test parameter.
     */
    @Getter
    @Setter
    @IgorParam
    private String testProviderParam = "provider-test-param";

    /**
     * An empty String parameter must be ignored during serialization.
     */
    @Getter
    @Setter
    @IgorParam
    private String emptyStringParam = "";

    /**
     * An empty Object parameter must be ignored during serialization.
     */
    @Getter
    @Setter
    @IgorParam
    private Integer nullInteger;

    /**
     * Creates a new component instance.
     */
    public TestProvider() {
        super(CATEGORY_ID, TYPE_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> next() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {

    }

}

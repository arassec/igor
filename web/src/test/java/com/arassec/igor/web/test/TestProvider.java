package com.arassec.igor.web.test;

import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.model.provider.BaseProvider;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
    @NotEmpty
    private String testProviderParam = "provider-test-param";

    /**
     * An empty String parameter must be ignored during serialization.
     */
    @Getter
    @Setter
    @IgorParam(subtype = ParameterSubtype.CRON)
    private String emptyStringParam = "";

    /**
     * An empty Object parameter must be ignored during serialization.
     */
    @Getter
    @Setter
    @IgorParam
    private Integer nullInteger;

    /**
     * An integer that is used for validation tests.
     */
    @Getter
    @Setter
    @IgorParam
    @NotNull
    private Integer validatedInteger;

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

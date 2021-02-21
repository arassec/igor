package com.arassec.igor.core.model.connector;

import com.arassec.igor.core.model.CoreCategory;
import com.arassec.igor.core.model.CoreType;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.annotation.validation.MissingComponent;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.util.IgorException;
import lombok.Getter;

/**
 * Placeholder connector for connectors, that could not be found by igor.
 */
public class MissingComponentConnector extends BaseConnector {

    /**
     * The problem that caused this connector to be used by igor.
     */
    @Getter
    @MissingComponent
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE)
    private final String errorCause;

    /**
     * Creates a new component instance.
     *
     * @param errorCause Description of the original error cause.
     */
    public MissingComponentConnector(String errorCause) {
        super(CoreCategory.CORE.getId(), CoreType.MISSING_COMPONENT_CONNECTOR.getId());
        this.errorCause = errorCause;
    }

    /**
     * Always throws an {@link IgorException} since this connector indicates a dependency problem.
     */
    @Override
    public void testConfiguration() {
        throw new IgorException("A component for this job could not be found! Please provide the required dependency or delete " +
                "this action/trigger.");
    }

}

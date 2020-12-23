package com.arassec.igor.core.model.trigger;

import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.util.validation.MissingComponent;
import lombok.Getter;

/**
 * Placeholder trigger for triggers, that could not be found by igor.
 */
public class MissingComponentTrigger extends BaseTrigger {

    /**
     * The problem that caused this trigger to be used by igor.
     */
    @Getter
    @MissingComponent
    @IgorParam(subtype = ParameterSubtype.MULTI_LINE)
    private final String errorCause;

    /**
     * Creates a new component instance.
     */
    public MissingComponentTrigger(String errorCause) {
        super("core", "missing-component-trigger");
        this.errorCause = errorCause;
    }

}

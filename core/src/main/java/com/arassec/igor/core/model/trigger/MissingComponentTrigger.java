package com.arassec.igor.core.model.trigger;

import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.annotation.validation.MissingComponent;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
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
     *
     * @param errorCause Description of the original error cause.
     */
    public MissingComponentTrigger(String errorCause) {
        this.errorCause = errorCause;
    }

}

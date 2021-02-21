package com.arassec.igor.core.model.action;

import com.arassec.igor.core.model.CoreCategory;
import com.arassec.igor.core.model.CoreType;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.annotation.validation.MissingComponent;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.util.IgorException;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Placeholder action for actions, that could not be found by igor.
 */
public class MissingComponentAction extends BaseAction {

    /**
     * The problem that caused this action to be used by igor.
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
    public MissingComponentAction(String errorCause) {
        super(CoreCategory.CORE.getId(), CoreType.MISSING_COMPONENT_ACTION.getId());
        getUnEditableProperties().add("numThreads");
        setActive(true);
        this.errorCause = errorCause;
    }

    /**
     * Always throws an {@link IgorException} since this action indicates a dependency problem!
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        throw new IgorException("A component for this job could not be found! Please provide the required dependency or delete " +
                "this action.");
    }

}

package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CorePluginType;
import com.arassec.igor.plugin.core.CoreUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * Filters the supplied data by a regular expression.
 */
@Slf4j
@Getter
@Setter
@IgorComponent
public class FilterByRegExpAction extends BaseUtilAction {

    /**
     * The input to test against the regular expression.
     */
    @NotBlank
    @IgorParam
    private String input;

    /**
     * The Regular expression to filter the input with.
     */
    @NotBlank
    @IgorParam
    private String expression;

    /**
     * If set to {@code true}, the filter will drop all matching data items. If set to {@code false}, all non-matching data items
     * will be dropped.
     */
    @IgorParam(advanced = true)
    private boolean dropMatching;

    /**
     * Creates a new component instance.
     */
    public FilterByRegExpAction() {
        super(CorePluginType.FILTER_BY_REGEXP_ACTION.getId());
    }

    /**
     * Matches the provided data against the configured regular expression and filters it, if it doesn't match.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job execution log.
     *
     * @return The supplied data, if the value under the configured {@link #input} matches the regular expresion. {@code null}
     * otherwise.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        String resolvedInput = CoreUtils.getString(data, input);
        String resolvedExpression = CoreUtils.getString(data, expression);

        if (resolvedInput == null || resolvedExpression == null) {
            log.debug("Missing required data for filtering: {} / {}", resolvedInput, resolvedExpression);
            return List.of();
        }

        if ((dropMatching && resolvedInput.matches(resolvedExpression))
                || (!dropMatching && !resolvedInput.matches(resolvedExpression))) {
            log.debug("Filtered '{}' against RegExp '{}'", resolvedInput, resolvedExpression);
            return List.of();
        }

        log.debug("Passed '{}' against RegExp '{}'", resolvedInput, resolvedExpression);
        return List.of(data);
    }

}

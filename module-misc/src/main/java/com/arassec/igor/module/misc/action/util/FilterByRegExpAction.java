package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * Filters the supplied data by a regular expression.
 */
@Slf4j
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
     * Creates a new component instance.
     */
    public FilterByRegExpAction() {
        super("b404544a-a145-440c-80a3-1017e2b193cf");
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

        String resolvedInput = getString(data, input);
        String resolvedExpression = getString(data, expression);

        if (resolvedInput == null || resolvedExpression == null) {
            log.debug("Missing required data for filtering: {} / {}", resolvedInput, resolvedExpression);
            return List.of();
        }

        if (!resolvedInput.matches(resolvedExpression)) {
            log.debug("Filtered '{}' against RegExp '{}'", resolvedInput, resolvedExpression);
            return List.of();
        }

        log.debug("Passed '{}' against RegExp '{}'", resolvedInput, resolvedExpression);
        return List.of(data);
    }

}

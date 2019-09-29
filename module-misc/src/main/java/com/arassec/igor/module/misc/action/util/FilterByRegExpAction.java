package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Filters the supplied data by a regular expression.
 */
@Slf4j
@IgorComponent("Filter by regular expression")
public class FilterByRegExpAction extends BaseUtilAction {

    /**
     * The input to test against the regular expression.
     */
    @IgorParam
    private String input;

    /**
     * The Regular expression to filter the input with.
     */
    @IgorParam
    private String expression;

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
            return null;
        }

        if (!resolvedInput.matches(resolvedExpression)) {
            log.debug("Filtered '{}' against RegExp '{}'", resolvedInput, resolvedExpression);
            return null;
        }

        log.debug("Passed '{}' against RegExp '{}'", resolvedInput, resolvedExpression);
        return List.of(data);
    }

}

package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.action.BaseAction;
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
     * The Regular expression to filter the input with.
     */
    @IgorParam
    private String expression;

    /**
     * Matches the provided data against the configured regular expression and filters it, if it doesn't match.
     *
     * @param data         The data the action will work with.
     * @param isDryRun     Unused - the action will always filter by regular expression.
     * @param jobExecution The job execution log.
     *
     * @return {@code true}, if the value under the configured {@link BaseAction#dataKey} matches the regular expresion, {@code
     * false} otherwise.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, boolean isDryRun, JobExecution jobExecution) {
        if (isValid(data)) {
            if (!getString(data, dataKey).matches(expression)) {
                log.debug("Filtered '{}' against RegExp '{}'", getString(data, dataKey), expression);
                return null;
            }
            log.debug("Passed '{}' against RegExp '{}'", getString(data, dataKey), expression);
            return List.of(data);
        }
        return null;
    }

}

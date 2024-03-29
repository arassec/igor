package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
import com.arassec.igor.plugin.core.CoreType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * <h2>'Filter by Regular Expression' Action</h2>
 *
 * <h3>Description</h3>
 * This action filters data items by evaluating a regular expression against a property value of the data item.<br>
 * <p>
 * If the regular expression matches, the data item is passed to the following action. Otherwise the data item is filtered and not
 * passed to the following action.
 *
 * <h3>Regular Expressions</h3>
 * This action uses Java's 'String.matches(String regExp)' method for regular expression matching. The regular expressions
 * supported by this method are described in
 * <a href="https://docs.oracle.com/javase/tutorial/essential/regex/">The Java Tutorials</a>.
 */
@Slf4j
@Getter
@Setter
@IgorComponent(categoryId = CoreCategory.UTIL, typeId = CoreType.FILTER_BY_REGEXP_ACTION)
public class FilterByRegExpAction extends BaseUtilAction {

    /**
     * A mustache expression selecting a property from the data item. The property's value is used for matching against the
     * regular expression.
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
     * If checked, data items that <strong>match</strong> the regular expression will be removed from the stream. If unchecked,
     * data items that <strong>do not</strong> match are removed from further processing.
     */
    @IgorParam(advanced = true)
    private boolean dropMatching;

    /**
     * Matches the provided data against the configured regular expression and filters it, if it doesn't match.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job execution log.
     * @return The supplied data, if the value under the configured {@link #input} matches the regular expresion. {@code null}
     * otherwise.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        var resolvedInput = CorePluginUtils.evaluateTemplate(data, input);
        var resolvedExpression = CorePluginUtils.evaluateTemplate(data, expression);

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

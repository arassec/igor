package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <h2>'Sort by Timestamp Pattern' Action</h2>
 *
 * <h3>Description</h3>
 * This action sorts data items based on a timestamp inside a value of the input data.<br>
 * <p>
 * This might e.g. be used to sort files by a timestamp that is part of their filename, if the timestamp of the last modification
 * is not available.
 *
 * <h3>Example</h3>
 * <p>
 * An example data item processed by this action might look like this:
 * <pre><code>
 * {
 *   "data": {
 *     "filename": "alpha_20200113185100_beta.jpeg",
 *     "directory": "/"
 *   },
 *   "meta": {
 *     "jobId": "1e91a654-ba8c-4c3a-afd0-932bd27d2888",
 *     "timestamp": 1587203554775
 *   }
 * }
 * </code></pre>
 * <p>
 * With the following configuration, all data items of a job execution could be sorted by the timestamp encoded in the
 * filename:<br>
 * <table>
 *     <caption>Example configuration</caption>
 *     <tr>
 *         <th>Parameter</th>
 *         <th>Configuration value</th>
 *     </tr>
 *     <tr>
 *         <td>Input</td>
 *         <td>$.data.filename</td>
 *     </tr>
 *     <tr>
 *         <td>Pattern</td>
 *         <td>[0-9]{14}</td>
 *     </tr>
 *     <tr>
 *         <td>Timestamp format</td>
 *         <td>yyyyMMddHHmmss</td>
 *     </tr>
 * </table>
 *
 * <h3>Event-Triggered Jobs</h3>
 * <strong>This action is not available in event-triggered jobs!</strong>
 * <p>
 * Sorting is done by collecting all data items of one job execution and by sorting the resulting list.
 * Since event triggered jobs don't stop their execution, the action would collect data items forever, and never forwarding them
 * to following actions.
 */
@Slf4j
@Getter
@Setter
@IgorComponent(typeId = "sort-by-timestamp-pattern-action", categoryId = CoreCategory.UTIL)
public class SortByTimestampPatternAction extends BaseUtilAction {

    /**
     * A mustache expression selecting a property from the data item. The property's value is converted into a timestamp and used
     * for sorting.
     */
    @NotBlank
    @IgorParam
    private String input;

    /**
     * A regular expression matching the timestamp part of the input value.
     */
    @NotBlank
    @IgorParam
    private String pattern;

    /**
     * The format of the timtestamp part of the property's value. See
     * <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html">Java
     * DateTimeFormat</a> (section 'Patterns for Formatting and Parsing') for allowed values.
     */
    @NotBlank
    @IgorParam
    private String timestampFormat = DEFAULT_TIMESTAMP_FORMAT;

    /**
     * If checked, data items are sorted from older to newer timestamps. If unchecked, data items are sorted from newer to older
     * timestamps.
     */
    @IgorParam
    private boolean sortAscending = true;

    /**
     * Contains all data that should have been processed by the action.
     */
    private List<Map<String, Object>> collectedData = new LinkedList<>();

    /**
     * Collects all data in memory for later sorting.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job execution log.
     *
     * @return Always an empty list.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {
        collectedData.add(data);
        return List.of();
    }

    /**
     * Sorts the collected data and returns the result.
     *
     * @return The sorted list of data to further process.
     */
    @Override
    public List<Map<String, Object>> complete() {

        final Pattern p;
        if (isTemplate(pattern)) {
            p = null;
        } else {
            p = Pattern.compile(pattern);
        }

        final DateTimeFormatter formatter;
        if (isTemplate(timestampFormat)) {
            formatter = null;
        } else {
            formatter = DateTimeFormatter.ofPattern(timestampFormat);
        }

        final boolean applyDefaultTimezone = (!pattern.contains("V") && !pattern.contains("z") && !pattern
            .contains("O") && !pattern.contains("X") && !pattern.contains("x") && !pattern.contains("Z"));

        if (!collectedData.isEmpty()) {
            return collectedData.stream()
                .filter(data -> extractDateTime(data, p, formatter, applyDefaultTimezone) != null)
                .sorted((o1, o2) -> {
                    ZonedDateTime firstDateTime = extractDateTime(o1, p, formatter, applyDefaultTimezone);
                    ZonedDateTime secondDateTime = extractDateTime(o2, p, formatter, applyDefaultTimezone);
                    if (sortAscending) {
                        return Objects.requireNonNull(firstDateTime).compareTo(Objects.requireNonNull(secondDateTime));
                    } else {
                        return Objects.requireNonNull(secondDateTime).compareTo(Objects.requireNonNull(firstDateTime));
                    }
                }).collect(Collectors.toList());
        }

        return List.of();
    }

    /**
     * Checks if a configured value is a mustache template or not.
     *
     * @param input The input to check.
     *
     * @return {@code true} if the input is a mustache template, {@code false} otherwise.
     */
    private boolean isTemplate(String input) {
        return input.contains("{{") && input.contains("}}");
    }

    /**
     * Extracts a {@link ZonedDateTime} from the input data.
     *
     * @param data                 The raw input data.
     * @param p                    The pattern to use to find the date-time-String.
     * @param formatter            The {@link ZonedDateTime} for the supplied String.
     * @param applyDefaultTimezone Set to {@code true}, if the format is in {@link LocalDateTime} and the system's default time
     *                             zone must be applied to the result.
     *
     * @return The {@link ZonedDateTime} or {@code null}, if none could be extracted.
     */
    private ZonedDateTime extractDateTime(Map<String, Object> data, Pattern p, DateTimeFormatter formatter,
                                          boolean applyDefaultTimezone) {

        var resolvedInput = CorePluginUtils.getString(data, input);
        if (resolvedInput == null) {
            log.debug("Missing data to extract date-time: {}", input);
            return null;
        }

        var rawValue = CorePluginUtils.getString(data, resolvedInput);

        Pattern pn = p;
        if (pn == null) {
            var resolvedPattern = CorePluginUtils.getString(data, pattern);
            if (resolvedPattern == null) {
                log.debug("Missing pattern to extract date-time: {}", pattern);
                return null;
            }
            pn = Pattern.compile(resolvedPattern);
        }

        DateTimeFormatter df = formatter;
        if (df == null) {
            var resolvedTimestampFormat = CorePluginUtils.getString(data, timestampFormat);
            if (resolvedTimestampFormat == null) {
                log.debug("Missing timestamp format to extract date-time: {}", timestampFormat);
                return null;
            }
            applyDefaultTimezone = (!resolvedTimestampFormat.contains("V") && !resolvedTimestampFormat.contains("z")
                && !resolvedTimestampFormat.contains("O") && !resolvedTimestampFormat.contains("X")
                && !resolvedTimestampFormat.contains("x") && !resolvedTimestampFormat.contains("Z"));
            df = DateTimeFormatter.ofPattern(resolvedTimestampFormat);
        }

        var m = pn.matcher(rawValue);

        if (m.find()) {
            if (applyDefaultTimezone) {
                return ZonedDateTime.of(LocalDateTime.parse(m.group(), df), ZoneId.systemDefault());
            } else {
                return ZonedDateTime.parse(m.group(), df);
            }

        }

        return null;
    }

    /**
     * Sorting works on all data items in the stream. With events, the stream is unlimited and thus this action would wait forever
     * for the job to finish.
     *
     * @return Always {@code false}.
     */
    @Override
    public boolean supportsEvents() {
        return false;
    }

    /**
     * Skipping is always done single-threaded.
     *
     * @return Always {@code true}.
     */
    @Override
    public boolean enforceSingleThread() {
        return true;
    }

}

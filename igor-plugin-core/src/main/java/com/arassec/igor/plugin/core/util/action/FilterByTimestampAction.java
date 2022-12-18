package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CorePluginUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * <h2>'Filter by Timestamp' Action</h2>
 *
 * <h3>Description</h3>
 * This action filters data items by comparing a timestamp from the input against a configured time span.<br>
 *
 * Filtered data items are not passed to following actions.
 *
 * <h3>Filtering Epoch Timestamps</h3>
 * If the timestamp is given as Epoch timestamp, you can use the following special values as 'Timestamp format' parameter:<br>
 *
 * <table>
 *     <caption>Timestamp formats</caption>
 *     <tr>
 *         <th>Timestamp format value</th>
 *         <th>Description</th>
 *     </tr>
 *     <tr>
 *         <td>epoch-millis</td>
 *         <td>Assumes the timestamp value is given as Epoch timestamp in milliseconds.</td>
 *     </tr>
 *     <tr>
 *         <td>epoch-seconds</td>
 *         <td>Assumes the timestamp value is given as Epoch timestamp in seconds.</td>
 *     </tr>
 * </table>
 */
@Slf4j
@Setter
@Getter
@IgorComponent(typeId = "filter-by-timestamp-action", categoryId = CoreCategory.UTIL)
public class FilterByTimestampAction extends BaseUtilAction {

    /**
     * Format value for epoch timestamps in milliseconds.
     */
    public static final String FORMAT_EPOCH_MILLIS = "epoch-millis";

    /**
     * Format value for epoch timestamps in seconds.
     */
    public static final String FORMAT_EPOCH_SECONDS = "epoch-seconds";

    /**
     * A mustache expression selecting a property from the data item. The property's value is converted into a timestamp and
     * afterwards used for filtering.
     */
    @NotBlank
    @IgorParam
    private String input;

    /**
     * If checked, data items with timestamps older than the configured time span are filtered. If unchecked, data items with
     * timestamps younger than the configured time span are filtered.
     */
    @IgorParam
    private boolean olderThan = true;

    /**
     * The amount of the time span.
     */
    @Positive
    @IgorParam
    private long amount = 1;

    /**
     * The unit of the time span. Allowed values are e.g. 'MINUTES', DAYS', 'WEEKS'. See
     * <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/temporal/ChronoUnit.html">Java
     * ChronoUnit</a> for all allowed values.
     */
    @NotBlank
    @IgorParam
    private String timeUnit = DEFAULT_TIME_UNIT;

    /**
     * The format of the property's value containing the timestamp. See
     * <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html">Java
     * DateTimeFormat</a> (section 'Patterns for Formatting and Parsing') for allowed values. The special values 'millis' and
     * 'seconds' are additionally supported (see below).
     */
    @NotBlank
    @IgorParam
    private String timestampFormat = DEFAULT_TIMESTAMP_FORMAT;

    /**
     * Filters data according to the configured timestamp settings.
     *
     * @param data         The data the action will work with.
     * @param jobExecution The job's execution log.
     *
     * @return The supplied data, if the timestamp under the configured {@link #input} matches the configured criteria. {@code
     * null} otherwise.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, JobExecution jobExecution) {

        var resolvedInput = CorePluginUtils.getString(data, input);
        var resolvedTimeUnit = CorePluginUtils.getString(data, timeUnit);
        var resolvedTimestampFormat = CorePluginUtils.getString(data, timestampFormat);

        if (resolvedInput == null || resolvedTimeUnit == null || resolvedTimestampFormat == null) {
            log.debug("Missing required data for filtering: {} / {} / {}", resolvedInput, resolvedTimeUnit, resolvedTimestampFormat);
            return List.of();
        }

        var target = LocalDateTime.now();
        target = target.minus(amount, ChronoUnit.valueOf(resolvedTimeUnit));

        LocalDateTime actual;
        if (FORMAT_EPOCH_MILLIS.equals(resolvedTimestampFormat)) {
            var instant = Instant.ofEpochMilli(Long.parseLong(resolvedInput));
            actual = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else if (FORMAT_EPOCH_SECONDS.equals(resolvedTimestampFormat)) {
            var instant = Instant.ofEpochSecond(Long.parseLong(resolvedInput));
            actual = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else {
            actual = LocalDateTime.parse(resolvedInput, DateTimeFormatter.ofPattern(resolvedTimestampFormat));
        }

        if (olderThan) {
            if (actual.isBefore(target)) {
                log.debug("Filtered '{}' against '{}' which is older than '{} {}'", actual, target, amount, resolvedTimeUnit);
                return List.of();
            }
            log.debug("Passed '{}' against '{}' which is younger than '{} {}'", actual, target, amount, resolvedTimeUnit);
        } else {
            if (actual.isAfter(target)) {
                log.debug("Filtered '{}' against '{}' which is younger than '{} {}'", actual, target, amount, resolvedTimeUnit);
                return List.of();
            }
            log.debug("Passed '{}' against '{}' which is older than '{} {}'", actual, target, amount, resolvedTimeUnit);
        }

        return List.of(data);
    }

}

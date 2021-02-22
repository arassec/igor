package com.arassec.igor.plugin.core.util.action;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.plugin.core.CorePluginType;
import com.arassec.igor.plugin.core.CorePluginUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Filters the supplied data by a timestamp.
 */
@Slf4j
@Setter
@Getter
@IgorComponent
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
     * The input to use as Timestamp.
     */
    @NotBlank
    @IgorParam
    private String input;

    /**
     * If set to {@code true}, timestamps older than the configured one are filtered. If set to {@code false}, timestamps younger
     * than the configured one are filtered.
     */
    @IgorParam(defaultValue = "true")
    private boolean olderThan;

    /**
     * The amount of time (configured by {@link #timeUnit}) to use for filtering.
     */
    @Positive
    @IgorParam(defaultValue = "1")
    private long amount;

    /**
     * The time unit to use for filtering.
     */
    @NotBlank
    @IgorParam(defaultValue = DEFAULT_TIME_UNIT)
    private String timeUnit;

    /**
     * The format of the timestamp.
     */
    @NotBlank
    @IgorParam(defaultValue = DEFAULT_TIMESTAMP_FORMAT)
    private String timestampFormat;

    /**
     * Creates a new component instance.
     */
    public FilterByTimestampAction() {
        super(CorePluginType.FILTER_BY_TIMESTAMP_ACTION.getId());
    }

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

        String resolvedInput = CorePluginUtils.getString(data, input);
        String resolvedTimeUnit = CorePluginUtils.getString(data, timeUnit);
        String resolvedTimestampFormat = CorePluginUtils.getString(data, timestampFormat);

        if (resolvedInput == null || resolvedTimeUnit == null || resolvedTimestampFormat == null) {
            log.debug("Missing required data for filtering: {} / {} / {}", resolvedInput, resolvedTimeUnit, resolvedTimestampFormat);
            return List.of();
        }

        LocalDateTime target = LocalDateTime.now();
        target = target.minus(amount, ChronoUnit.valueOf(resolvedTimeUnit));

        LocalDateTime actual;
        if (FORMAT_EPOCH_MILLIS.equals(resolvedTimestampFormat)) {
            Instant instant = Instant.ofEpochMilli(Long.parseLong(resolvedInput));
            actual = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else if (FORMAT_EPOCH_SECONDS.equals(resolvedTimestampFormat)) {
            Instant instant = Instant.ofEpochSecond(Long.parseLong(resolvedInput));
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

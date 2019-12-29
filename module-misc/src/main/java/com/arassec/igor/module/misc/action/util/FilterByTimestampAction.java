package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Filters the supplied data by a regular expression.
 */
@Slf4j
@Setter
@Getter
@IgorComponent
public class FilterByTimestampAction extends BaseUtilAction {

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
    @IgorParam
    private boolean olderThan = true;

    /**
     * The amount of time (configured by {@link #timeUnit}) to use for filtering.
     */
    @PositiveOrZero
    @IgorParam
    private long amount;

    /**
     * The time unit to use for filtering.
     */
    @NotBlank
    @IgorParam
    private String timeUnit = ChronoUnit.DAYS.name();

    /**
     * The format of the timestamp.
     */
    @NotBlank
    @IgorParam
    private String timestampFormat = defaultTimeFormat;

    /**
     * The optional time zone of the timestamp.
     */
    @IgorParam(optional = true)
    private String timezone;

    /**
     * Creates a new component instance.
     */
    public FilterByTimestampAction() {
        super("aca4d78f-ad37-451c-baab-0533c5735333");
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

        String resolvedInput = getString(data, input);
        String resolvedTimeUnit = getString(data, timeUnit);
        String resolvedTimestampFormat = getString(data, timestampFormat);
        String resolvedTimezone = getString(data, timezone);

        if (resolvedInput == null || resolvedTimeUnit == null || resolvedTimestampFormat == null) {
            log.debug("Missing required data for filtering: {} / {} / {}", resolvedInput, resolvedTimeUnit, resolvedTimestampFormat);
            return List.of();
        }

        LocalDateTime target;
        if (resolvedTimezone != null) {
            target = LocalDateTime.now(ZoneId.of(resolvedTimezone));
        } else {
            target = LocalDateTime.now();
        }

        target = target.minus(amount, ChronoUnit.valueOf(resolvedTimeUnit));

        LocalDateTime actual = LocalDateTime.parse(resolvedInput, DateTimeFormatter.ofPattern(resolvedTimestampFormat));

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

package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
@Component
@Scope("prototype")
public class FilterByTimestamp extends BaseUtilAction {

    /**
     * The input to use as Timestamp.
     */
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
    @IgorParam
    private long amount;

    /**
     * The time unit to use for filtering.
     */
    @IgorParam
    private String timeUnit = ChronoUnit.DAYS.name();

    /**
     * The format of the timestamp.
     */
    @IgorParam
    private String timestampFormat = TIME_FORMAT;

    /**
     * The optional time zone of the timestamp.
     */
    @IgorParam(optional = true)
    private String timezone;

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
            return null;
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
            if (actual.isAfter(target)) {
                log.debug("Filtered '{}' which is older than {} {}", actual, amount, resolvedTimeUnit);
                return null;
            }
        } else {
            if (actual.isBefore(target)) {
                log.debug("Filtered '{}' which is younger than {} {}", actual, amount, resolvedTimeUnit);
                return null;
            }
        }

        log.debug("Passed '{}' against older={}, {}, {}", actual, olderThan, amount, resolvedTimeUnit);
        return List.of(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeId() {
        return "aca4d78f-ad37-451c-baab-0533c5735333";
    }

}

package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.extern.slf4j.Slf4j;

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
@IgorAction(label = "Filter by timestamp")
public class FilterByTimestamp extends BaseUtilAction {

    /**
     * If set to {@code true}, timestamps older than the configured one are filtered. If set to {@code false}, timestamps
     * younger than the configured one are filtered.
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
     * @param isDryRun     Unused - the action always filters by timestamp.
     * @param jobExecution The job's execution log.
     * @return
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, boolean isDryRun, JobExecution jobExecution) {
        if (isValid(data)) {
            LocalDateTime target;
            if (timezone != null) {
                target = LocalDateTime.now(ZoneId.of(timezone));
            } else {
                target = LocalDateTime.now();
            }

            target = target.minus(amount, ChronoUnit.valueOf(timeUnit));

            LocalDateTime actual = LocalDateTime.parse(getString(data, dataKey), DateTimeFormatter.ofPattern(timestampFormat));

            if (olderThan) {
                if (actual.isAfter(target)) {
                    log.debug("Filtered '{}' which is older than {} {}", actual, amount, timeUnit);
                    return null;
                }
            } else {
                if (actual.isBefore(target)) {
                    log.debug("Filtered '{}' which is younger than {} {}", actual, amount, timeUnit);
                    return null;
                }
            }
            log.debug("Passed '{}' against older={}, {}, {}", actual, olderThan, amount, timeUnit);
            return List.of(data);
        }
        return null;
    }

}

package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Filters the supplied data by a regular expression.
 */
@IgorAction(label = "Filter by timestamp")
public class FilterByTimestamp extends BaseUtilAction {

    @IgorParam
    private boolean olderThan = true;

    @IgorParam
    private long amount;

    @IgorParam
    private String timeUnit = ChronoUnit.DAYS.name();

    @IgorParam
    private String timestampFormat = TIME_FORMAT;

    @IgorParam(optional = true)
    private String timezone;

    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, boolean isDryRun) {
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
                if (actual.isBefore(target)) {
                    return null;
                }
            } else {
                if (actual.isAfter(target)) {
                    return null;
                }
            }
        }
        return List.of(data);
    }

}

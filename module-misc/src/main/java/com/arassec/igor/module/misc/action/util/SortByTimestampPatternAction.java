package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Sorts data by using a configurable pattern on a data value.
 */
@IgorAction(label = "Sort by timestamp pattern")
@Slf4j
public class SortByTimestampPatternAction extends BaseUtilAction {

    /**
     * The pattern to use to extract the date from the target value.
     */
    @IgorParam
    private String pattern;

    /**
     * The timestamp format.
     */
    @IgorParam
    private String timestampFormat = TIME_FORMAT;

    /**
     * Defines the sort order
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
     * @param data     The data the action will work with.
     * @param isDryRun {@code true} if the data should be processed in an idempotent way, i.e. the data should not be
     *                 changed irreversably. Set to {@code false} to process the data regularly according to the actions
     *                 purpose.
     * @return Always {@code null}.
     */
    @Override
    public List<Map<String, Object>> process(Map<String, Object> data, boolean isDryRun) {
        if (isValid(data)) {
            collectedData.add(data);
        }
        return null;
    }

    /**
     * Sorts the collected data and returns the result.
     *
     * @return The sorted list of data to further process.
     */
    @Override
    public List<Map<String, Object>> complete() {
        Pattern p = Pattern.compile(pattern);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timestampFormat);
        final boolean applyDefaultTimezone =
                (!pattern.contains("V") && !pattern.contains("z") && !pattern.contains("O") && !pattern.contains("X") && !pattern.contains("x") && !pattern.contains("Z"));

        if (!collectedData.isEmpty()) {
            List<Map<String, Object>> result = collectedData.stream().filter(data -> extractDateTime(data, p, formatter,
                    applyDefaultTimezone) != null).collect(Collectors.toList());
            Collections.sort(result, (o1, o2) -> {
                ZonedDateTime firstDateTime = extractDateTime(o1, p, formatter, applyDefaultTimezone);
                ZonedDateTime secondDateTime = extractDateTime(o2, p, formatter, applyDefaultTimezone);
                if (sortAscending) {
                    return firstDateTime.compareTo(secondDateTime);
                } else {
                    return secondDateTime.compareTo(firstDateTime);
                }
            });
            return result;
        }

        return null;
    }

    /**
     * Extracts a {@link ZonedDateTime} from the input data.
     *
     * @param data                 The raw input data.
     * @param p                    The pattern to use to find the date-time-String.
     * @param formatter            The {@link ZonedDateTime} for the supplied String.
     * @param applyDefaultTimezone Set to {@code true}, if the format is in {@link java.time.LocalDateTime} and the system's
     *                             default
     *                             time zone must be applied to the result.
     * @return The {@link ZonedDateTime} or {@ocde null}, if none could be extracted.
     */
    private ZonedDateTime extractDateTime(Map<String, Object> data, Pattern p, DateTimeFormatter formatter,
                                          boolean applyDefaultTimezone) {
        String rawValue = getString(data, dataKey);
        Matcher m = p.matcher(rawValue);
        if (m.find()) {
            if (applyDefaultTimezone) {
                return ZonedDateTime.of(LocalDateTime.parse(m.group(), formatter), ZoneId.systemDefault());
            } else {
                return ZonedDateTime.parse(m.group(), formatter);
            }

        }
        return null;
    }

    /**
     * Returns always 1, because the action needs to collect all data to sort in a single thread.
     *
     * @return Always {@code 1}.
     */
    @Override
    public int getNumThreads() {
        return 1;
    }

}

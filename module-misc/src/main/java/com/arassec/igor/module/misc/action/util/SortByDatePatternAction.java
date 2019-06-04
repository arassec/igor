package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.IgorParam;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sorts data by using a configurable pattern on a data value.
 */
@IgorAction(label = "Sort by date pattern")
@Slf4j
public class SortByDatePatternAction extends BaseUtilAction {

    /**
     * The pattern to use to extract the date from the target value.
     */
    @IgorParam
    private String pattern;

    /**
     * The date format.
     */
    @IgorParam
    private String dateFormat;

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);

        if (!collectedData.isEmpty()) {
            Collections.sort(collectedData, (o1, o2) -> {
                LocalDateTime firstDateTime = extractDateTime(o1, p, formatter);
                LocalDateTime secondDateTime = extractDateTime(o2, p, formatter);
                if (firstDateTime != null && secondDateTime != null) {
                    return firstDateTime.compareTo(secondDateTime);
                }
                return 0;
            });
            return collectedData;
        }

        return null;
    }

    /**
     * Extracts the {@link LocalDateTime} from the input data.
     *
     * @param data      The raw input data.
     * @param p         The pattern to use to find the date-time-String.
     * @param formatter The {@link DateTimeFormatter} for the String.
     * @return The {@link LocalDateTime} or {@ocde null}, if none could be extracted.
     */
    private LocalDateTime extractDateTime(Map<String, Object> data, Pattern p, DateTimeFormatter formatter) {
        String rawValue = getString(data, dataKey);
        Matcher m = p.matcher(rawValue);
        if (m.find()) {
            return LocalDateTime.parse(m.group(), formatter);
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

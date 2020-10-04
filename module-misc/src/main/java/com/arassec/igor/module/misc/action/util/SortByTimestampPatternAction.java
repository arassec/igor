package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Sorts data by using a configurable pattern on a data value.
 */
@Slf4j
@Getter
@Setter
@IgorComponent
public class SortByTimestampPatternAction extends BaseUtilAction {

    /**
     * The input containing the timestamp to sort by.
     */
    @NotBlank
    @IgorParam
    private String input;

    /**
     * The pattern to use to extract the date from the target value.
     */
    @NotBlank
    @IgorParam
    private String pattern;

    /**
     * The timestamp format.
     */
    @NotBlank
    @IgorParam(defaultValue = DEFAULT_TIMESTAMP_FORMAT)
    private String timestampFormat;

    /**
     * Defines the sort order
     */
    @IgorParam(defaultValue = "true")
    private boolean sortAscending;

    /**
     * Contains all data that should have been processed by the action.
     */
    private List<Map<String, Object>> collectedData = new LinkedList<>();

    /**
     * Creates a new instance.
     */
    public SortByTimestampPatternAction() {
        super("sort-by-timestamp-pattern-action");
        // Sorting is always done single threaded:
        setNumThreads(1);
        getUnEditableProperties().add("numThreads");
    }

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
     * @param applyDefaultTimezone Set to {@code true}, if the format is in {@link java.time.LocalDateTime} and the system's
     *                             default time zone must be applied to the result.
     *
     * @return The {@link ZonedDateTime} or {@code null}, if none could be extracted.
     */
    private ZonedDateTime extractDateTime(Map<String, Object> data, Pattern p, DateTimeFormatter formatter,
                                          boolean applyDefaultTimezone) {

        String resolvedInput = getString(data, input);
        if (resolvedInput == null) {
            log.debug("Missing data to extract date-time: {}", input);
            return null;
        }

        String rawValue = getString(data, resolvedInput);

        Pattern pn = p;
        if (pn == null) {
            String resolvedPattern = getString(data, pattern);
            if (resolvedPattern == null) {
                log.debug("Missing pattern to extract date-time: {}", pattern);
                return null;
            }
            pn = Pattern.compile(resolvedPattern);
        }

        DateTimeFormatter df = formatter;
        if (df == null) {
            String resolvedTimestampFormat = getString(data, timestampFormat);
            if (resolvedTimestampFormat == null) {
                log.debug("Missing timestamp format to extract date-time: {}", timestampFormat);
                return null;
            }
            applyDefaultTimezone = (!resolvedTimestampFormat.contains("V") && !resolvedTimestampFormat.contains("z")
                    && !resolvedTimestampFormat.contains("O") && !resolvedTimestampFormat.contains("X")
                    && !resolvedTimestampFormat.contains("x") && !resolvedTimestampFormat.contains("Z"));
            df = DateTimeFormatter.ofPattern(resolvedTimestampFormat);
        }

        Matcher m = pn.matcher(rawValue);

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
     * Returns always 1, because the action needs to collect all data to sort in a single thread.
     *
     * @return Always {@code 1}.
     */
    @Override
    public int getNumThreads() {
        return 1;
    }

    /**
     * Deletes all processed data items so that sorting can begin with the next data item.
     */
    @Override
    public void reset() {
        collectedData.clear();
    }


}

package com.arassec.igor.module.misc.action.util;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.module.misc.action.BaseMiscActionTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link FilterByTimestampAction}
 */
class FilterByTimestampActionTest extends BaseMiscActionTest {

    /**
     * Tests a misconfigured action.
     */
    @Test
    @DisplayName("Tests invalid input handling.")
    void testInvalidConfiguration() {
        FilterByTimestampAction action = new FilterByTimestampAction();
        action.setTimeUnit(ChronoUnit.DAYS.name());
        action.setTimestampFormat("yyyy-MM-dd'T'00:00:00");

        // input missing:
        List<Map<String, Object>> processedData = action.process(createData(), new JobExecution());
        assertTrue(processedData.isEmpty());

        // timeunit missing:
        action.setInput("2019-12-29T00:00:00");
        action.setTimeUnit(null);
        processedData = action.process(createData(), new JobExecution());
        assertTrue(processedData.isEmpty());

        // timestamp format missing:
        action.setTimeUnit(ChronoUnit.DAYS.name());
        action.setTimestampFormat(null);
        processedData = action.process(createData(), new JobExecution());
        assertTrue(processedData.isEmpty());
    }

    /**
     * Tests filtering older entries.
     */
    @Test
    @DisplayName("Tests filtering older entries.")
    void testOlderThan() {
        FilterByTimestampAction action = new FilterByTimestampAction();
        action.setInput("$.timestamp");
        action.setAmount(1);
        action.setTimeUnit(ChronoUnit.DAYS.name());
        action.setTimestampFormat("yyyy-MM-dd'T'HH:mm:ss");

        Map<String, Object> data = createData();
        data.put("timestamp", DateTimeFormatter.ofPattern(action.getTimestampFormat()).format(LocalDateTime.now()));
        List<Map<String, Object>> processedData = action.process(data, new JobExecution());
        assertEquals(data, processedData.get(0));

        // Filter all older than 15 days:
        action.setAmount(15);

        processedData = action.process(data, new JobExecution());
        assertEquals(data, processedData.get(0));

        data.put("timestamp", "2014-04-13T03:00:00");
        processedData = action.process(data, new JobExecution());
        assertTrue(processedData.isEmpty());
    }

    /**
     * Tests filtering younger entries.
     */
    @Test
    @DisplayName("Tests filtering younger entries.")
    void testYoungerThan() {
        FilterByTimestampAction action = new FilterByTimestampAction();
        action.setOlderThan(false);
        action.setInput("$.timestamp");
        action.setAmount(1);
        action.setTimeUnit(ChronoUnit.HOURS.name());
        action.setTimestampFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

        Map<String, Object> data = createData();
        data.put("timestamp", DateTimeFormatter.ofPattern(action.getTimestampFormat()).format(ZonedDateTime.now()));
        List<Map<String, Object>> processedData = action.process(data, new JobExecution());
        assertTrue(processedData.isEmpty());

        data.put("timestamp", "2014-04-13T03:00:00+02:00");
        processedData = action.process(data, new JobExecution());
        assertEquals(data, processedData.get(0));
    }

    /**
     * Tests filtering within a configured timezone.
     */
    @Test
    @DisplayName("Tests filtering with timeszone data.")
    void testWithTimezone() {
        FilterByTimestampAction action = new FilterByTimestampAction();
        action.setInput("$.timestamp");
        action.setAmount(2);
        action.setTimeUnit(ChronoUnit.HOURS.name());
        action.setTimestampFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        action.setTimezone("$.timezone");

        Map<String, Object> data = createData();
        data.put("timezone", "-02:00");
        data.put("timestamp", DateTimeFormatter.ofPattern(action.getTimestampFormat()).format(ZonedDateTime.now()));

        List<Map<String, Object>> processedData = action.process(data, new JobExecution());
        assertEquals(data, processedData.get(0));

        data.put("timestamp",
                DateTimeFormatter.ofPattern(action.getTimestampFormat()).format(ZonedDateTime.now(ZoneId.of("-02:00"))
                        .minus(action.getAmount() + 1, ChronoUnit.HOURS)));
        processedData = action.process(data, new JobExecution());
        assertTrue(processedData.isEmpty());
    }

}
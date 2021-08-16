package com.arassec.igor.core.model.trigger;

import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.execution.JobExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link BaseTrigger}.
 */
@DisplayName("Base-Trigger Tests.")
class BaseTriggerTest {

    /**
     * The base class under test.
     */
    private final BaseTrigger baseTrigger = mock(BaseTrigger.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    /**
     * Tests initialization of the base trigger.
     */
    @Test
    @DisplayName("Tests initialization of the base trigger.")
    void testInitialize() {
        assertNull(ReflectionTestUtils.getField(baseTrigger, "jobExecution"));
        JobExecution jobExecution = JobExecution.builder().build();
        baseTrigger.initialize(jobExecution);
        assertEquals(jobExecution, ReflectionTestUtils.getField(baseTrigger, "jobExecution"));
    }

    /**
     * Tests creating the initial data item.
     */
    @Test
    @DisplayName("Tests creating the initial data item.")
    @SuppressWarnings("unchecked")
    void testCreateDataItem() {
        JobExecution jobExecution = JobExecution.builder().jobId("job-id").build();
        baseTrigger.initialize(jobExecution);

        Map<String, Object> dataItem = baseTrigger.createDataItem();
        assertTrue(dataItem.containsKey(DataKey.DATA.getKey()));
        assertTrue(dataItem.containsKey(DataKey.META.getKey()));

        Map<String, Object> meta = (Map<String, Object>) dataItem.get(DataKey.META.getKey());
        assertEquals("job-id", meta.get(DataKey.JOB_ID.getKey()));
        assertNotNull(meta.get(DataKey.TIMESTAMP.getKey()));
        assertEquals(false, meta.get(DataKey.SIMULATION.getKey()));
    }

}

package com.arassec.igor.core.util.event;

import com.arassec.igor.core.model.job.Job;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link JobEvent}.
 */
@DisplayName("Job-Event tests")
class JobEventTest {

    /**
     * Tests creation and property handling of events.
     */
    @Test
    @DisplayName("Tests creation and property handling of events.")
    void testCreationAndPropertyHandling() {
        Job job = Job.builder().build();
        JobEvent event = new JobEvent(JobEventType.STATE_CHANGE, job);
        assertEquals(JobEventType.STATE_CHANGE, event.getType());
        assertEquals(job, event.getJob());
    }

}

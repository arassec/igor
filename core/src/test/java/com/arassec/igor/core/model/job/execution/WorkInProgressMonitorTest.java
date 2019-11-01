package com.arassec.igor.core.model.job.execution;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link WorkInProgressMonitor} model.
 */
@DisplayName("WIP-Monitor Tests")
class WorkInProgressMonitorTest {

    /**
     * Tests storing data in the monitor.
     */
    @Test
    @DisplayName("Tests data storage.")
    void testDataStorage() {
        WorkInProgressMonitor wipMon = new WorkInProgressMonitor("test", 66);
        assertEquals("test", wipMon.getName());
        assertEquals(66, wipMon.getProgressInPercent());
    }

}

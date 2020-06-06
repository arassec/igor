package com.arassec.igor.module.file.connector.ssh;

import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link IgorSftpProgressMonitor}.
 */
class IgorSftpProgressMonitorTest {

    /**
     * Tests the progress monitor's functionality.
     */
    @Test
    @DisplayName("Tests the progress monitor's functionality.")
    void testProgressMonitor() {
        WorkInProgressMonitor wipMon = new WorkInProgressMonitor();
        IgorSftpProgressMonitor progressMonitor = new IgorSftpProgressMonitor(666, wipMon);

        progressMonitor.init(0, "src", "dest", 0);

        assertEquals("dest", ReflectionTestUtils.getField(progressMonitor, "target"));

        assertTrue(progressMonitor.count(333));
        assertEquals(50, wipMon.getProgressInPercent());
        assertFalse(progressMonitor.count(334));
        assertEquals(100, wipMon.getProgressInPercent());

        assertDoesNotThrow(progressMonitor::end);
    }

}
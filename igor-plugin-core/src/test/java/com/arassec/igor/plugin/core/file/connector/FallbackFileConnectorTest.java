package com.arassec.igor.plugin.core.file.connector;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the {@link FallbackFileConnector}.
 */
@DisplayName("Fallback-File-Connector Tests.")
class FallbackFileConnectorTest {

    /**
     * Tests method invocations.
     */
    @Test
    @DisplayName("Tests that every method invocation throws an exception.")
    void testMethodInvocation() {
        FallbackFileConnector connector = new FallbackFileConnector();

        assertThrows(IllegalStateException.class, () -> connector.listFiles(null, null));
        assertThrows(IllegalStateException.class, () -> connector.listFiles("", ""));

        assertThrows(IllegalStateException.class, () -> connector.read(null));
        assertThrows(IllegalStateException.class, () -> connector.read(""));

        assertThrows(IllegalStateException.class, () -> connector.readStream(null));
        assertThrows(IllegalStateException.class, () -> connector.readStream(""));

        FileStreamData fileStreamData = new FileStreamData();
        WorkInProgressMonitor wipMon = new WorkInProgressMonitor();
        JobExecution jobExecution = new JobExecution();

        assertThrows(IllegalStateException.class, () -> connector.writeStream(null, null, null, null));
        assertThrows(IllegalStateException.class, () -> connector.writeStream("", fileStreamData, wipMon, jobExecution));

        assertThrows(IllegalStateException.class, () -> connector.move(null, null));
        assertThrows(IllegalStateException.class, () -> connector.move("", ""));

        assertThrows(IllegalStateException.class, () -> connector.delete(null));
        assertThrows(IllegalStateException.class, () -> connector.delete(""));

        assertThrows(IllegalStateException.class, connector::testConfiguration);
    }

}
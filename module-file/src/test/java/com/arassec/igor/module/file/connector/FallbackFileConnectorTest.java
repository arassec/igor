package com.arassec.igor.module.file.connector;

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

        assertThrows(IllegalStateException.class, () -> connector.read(null, null));
        assertThrows(IllegalStateException.class, () -> connector.read("", new WorkInProgressMonitor("")));

        assertThrows(IllegalStateException.class, () -> connector.readStream(null, null));
        assertThrows(IllegalStateException.class, () -> connector.readStream("", new WorkInProgressMonitor("")));

        assertThrows(IllegalStateException.class, () -> connector.writeStream(null, null, null));
        assertThrows(IllegalStateException.class, () -> connector.writeStream("", new FileStreamData(), new WorkInProgressMonitor("")));

        assertThrows(IllegalStateException.class, () -> connector.move(null, null, null));
        assertThrows(IllegalStateException.class, () -> connector.move("", "", new WorkInProgressMonitor("")));

        assertThrows(IllegalStateException.class, () -> connector.delete(null, null));
        assertThrows(IllegalStateException.class, () -> connector.delete("", new WorkInProgressMonitor("")));

        assertThrows(IllegalStateException.class, connector::testConfiguration);
    }

}
package com.arassec.igor.module.file.service;

import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link FallbackFileService}.
 */
@DisplayName("Fallback-File-Service Tests.")
class FallbackFileServiceTest {

    /**
     * Tests method invocations.
     */
    @Test
    @DisplayName("Tests that every method invocation throws an exception.")
    void testMethodInvocation() {
        FallbackFileService service = new FallbackFileService();

        assertThrows(IllegalStateException.class, () -> service.listFiles(null, null));
        assertThrows(IllegalStateException.class, () -> service.listFiles("", ""));

        assertThrows(IllegalStateException.class, () -> service.read(null, null));
        assertThrows(IllegalStateException.class, () -> service.read("", new WorkInProgressMonitor("")));

        assertThrows(IllegalStateException.class, () -> service.readStream(null, null));
        assertThrows(IllegalStateException.class, () -> service.readStream("", new WorkInProgressMonitor("")));

        assertThrows(IllegalStateException.class, () -> service.writeStream(null, null, null));
        assertThrows(IllegalStateException.class, () -> service.writeStream("", new FileStreamData(), new WorkInProgressMonitor("")));

        assertThrows(IllegalStateException.class, () -> service.move(null, null, null));
        assertThrows(IllegalStateException.class, () -> service.move("", "", new WorkInProgressMonitor("")));

        assertThrows(IllegalStateException.class, () -> service.delete(null, null));
        assertThrows(IllegalStateException.class, () -> service.delete("", new WorkInProgressMonitor("")));

        assertThrows(IllegalStateException.class, service::testConfiguration);
    }

}
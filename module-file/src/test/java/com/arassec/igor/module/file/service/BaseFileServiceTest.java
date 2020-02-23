package com.arassec.igor.module.file.service;

import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link BaseFileService}.
 */
@DisplayName("Base-FileService Tests")
class BaseFileServiceTest {

    /**
     * The class under test.
     */
    private final BaseFileService baseFileService = mock(BaseFileService.class, CALLS_REAL_METHODS);

    /**
     * Tests finalizing a stream.
     */
    @Test
    @DisplayName("Tests finalizing a stream.")
    void testFinalize() {
        assertDoesNotThrow(() -> baseFileService.finalizeStream(null));
        assertDoesNotThrow(() -> baseFileService.finalizeStream(new FileStreamData()));
    }

    /**
     * Tests formatting a timestamp.
     */
    @Test
    @DisplayName("Tests formatting a timestamp.")
    void testFormatInstant() {
        assertNull(baseFileService.formatInstant(null));

        LocalDate date = LocalDate.parse("2020-02-22");
        Instant instant = date.atStartOfDay(ZoneId.of("Europe/Berlin")).toInstant();

        assertEquals("2020-02-22T00:00:00+01:00", baseFileService.formatInstant(instant));

        baseFileService.setTimezone("Etc/UCT");

        assertEquals("2020-02-21T23:00:00Z", baseFileService.formatInstant(instant));
    }

    /**
     * Tests copying a stream.
     */
    @Test
    @DisplayName("Tests copying a stream.")
    void testCopyStream() {
        InputStream inputStream = new ByteArrayInputStream("1234567890".getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        WorkInProgressMonitor workInProgressMonitor = new WorkInProgressMonitor("copy-stream-test");

        baseFileService.copyStream(inputStream, outputStream, 10, workInProgressMonitor);

        assertEquals(100, workInProgressMonitor.getProgressInPercent());
        assertEquals("1234567890", outputStream.toString());
    }

}
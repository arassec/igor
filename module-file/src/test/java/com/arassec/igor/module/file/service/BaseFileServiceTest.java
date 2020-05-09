package com.arassec.igor.module.file.service;

import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.util.IgorException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
     * Tests copying a stream.
     */
    @Test
    @DisplayName("Tests copying a stream.")
    void testCopyStream() {
        InputStream inputStream = new ByteArrayInputStream("1234567890".getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        WorkInProgressMonitor workInProgressMonitor = new WorkInProgressMonitor("copy-stream-test");

        baseFileService.setStreamCopyBufferSize(1024*1024);

        baseFileService.copyStream(inputStream, outputStream, 10, workInProgressMonitor);

        assertEquals(100, workInProgressMonitor.getProgressInPercent());
        assertEquals("1234567890", outputStream.toString());
    }

    /**
     * Tests copying streams with content larger than the configured buffer size.
     */
    @Test
    @DisplayName("Tests copying stream with a small buffer.")
    void testCopyStreamSmallBuffer() {
        InputStream inputStream = new ByteArrayInputStream("1234567890".getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        WorkInProgressMonitor workInProgressMonitor = new WorkInProgressMonitor("copy-stream-test");

        baseFileService.setStreamCopyBufferSize(2);

        baseFileService.copyStream(inputStream, outputStream, 10, workInProgressMonitor);

        assertEquals(100, workInProgressMonitor.getProgressInPercent());
        assertEquals("1234567890", outputStream.toString());
    }

    /**
     * Tests exception handling during stream copy.
     */
    @Test
    @DisplayName("Tests exception handling during stream copy.")
    @SneakyThrows
    void testCopyStreamFail() {
        InputStream inputStreamMock = mock(InputStream.class);
        when(inputStreamMock.read(any(byte[].class), eq(0), anyInt())).thenThrow(new IOException("base-file-service" +
                "-test-exception"));

        assertThrows(IgorException.class, () -> baseFileService.copyStream(inputStreamMock, null, 100,
                new WorkInProgressMonitor("test")));

        when(inputStreamMock.read(any(byte[].class), eq(0), anyInt())).thenReturn(-1);

        assertThrows(IgorException.class, () -> baseFileService.copyStream(inputStreamMock, null, 100,
                new WorkInProgressMonitor("test")));
    }

    /**
     * Tests formatting an instance.
     */
    @Test
    @DisplayName("Tests formatting an instance.")
    void testFormatInstance() {
        assertNull(baseFileService.formatInstant(null));
        Instant instant = Instant.ofEpochMilli(1234567890);
        String formattedInstant = baseFileService.formatInstant(instant);
        assertTrue(formattedInstant.startsWith("1970-01-15"));
        assertTrue(formattedInstant.contains(":56:07.89"));
    }

}
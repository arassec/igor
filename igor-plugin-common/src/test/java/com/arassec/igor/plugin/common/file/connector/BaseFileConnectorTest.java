package com.arassec.igor.plugin.common.file.connector;

import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.util.IgorException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link BaseFileConnector}.
 */
@DisplayName("Base-FileConnector Tests")
class BaseFileConnectorTest {

    /**
     * The class under test.
     */
    private final BaseFileConnector baseFileConnector = mock(BaseFileConnector.class, CALLS_REAL_METHODS);

    /**
     * Tests finalizing a stream.
     */
    @Test
    @DisplayName("Tests finalizing a stream.")
    void testFinalize() {
        assertDoesNotThrow(() -> baseFileConnector.finalizeStream(null));
        assertDoesNotThrow(() -> baseFileConnector.finalizeStream(new FileStreamData()));
    }

    /**
     * Tests copying a stream.
     */
    @Test
    @DisplayName("Tests copying a stream.")
    void testCopyStream() {
        InputStream inputStream = new ByteArrayInputStream("1234567890".getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        WorkInProgressMonitor workInProgressMonitor = new WorkInProgressMonitor();

        baseFileConnector.setStreamCopyBufferSize(1024 * 1024);

        baseFileConnector.copyStream(inputStream, outputStream, 10, workInProgressMonitor,
                JobExecution.builder().executionState(JobExecutionState.RUNNING).build());

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

        WorkInProgressMonitor workInProgressMonitor = new WorkInProgressMonitor();

        baseFileConnector.setStreamCopyBufferSize(2);

        baseFileConnector.copyStream(inputStream, outputStream, 10, workInProgressMonitor,
                JobExecution.builder().executionState(JobExecutionState.RUNNING).build());

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
        when(inputStreamMock.read(any(byte[].class), eq(0), anyInt())).thenThrow(new IOException("base-file-connector" +
                "-test-exception"));

        WorkInProgressMonitor wipMon = new WorkInProgressMonitor();
        JobExecution jobExecution = JobExecution.builder().executionState(JobExecutionState.RUNNING).build();

        assertThrows(IgorException.class, () -> baseFileConnector.copyStream(inputStreamMock, null, 100, wipMon, jobExecution));

        when(inputStreamMock.read(any(byte[].class), eq(0), anyInt())).thenReturn(-1);

        assertThrows(IgorException.class, () -> baseFileConnector.copyStream(inputStreamMock, null, 100, wipMon, jobExecution));
    }

    /**
     * Tests formatting an instance.
     */
    @Test
    @DisplayName("Tests formatting an instance.")
    void testFormatInstance() {
        assertNull(baseFileConnector.formatInstant(null));
        Instant instant = Instant.ofEpochMilli(1234567890);
        String formattedInstant = baseFileConnector.formatInstant(instant);
        assertTrue(formattedInstant.startsWith("1970-01-15"));
        assertTrue(formattedInstant.contains(":56:07.89"));
    }

    /**
     * Tests aborting file transfer if the job is cancelled.
     */
    @Test
    @DisplayName("Tests aborting file transfer if the job is cancelled.")
    @SneakyThrows
    void testCopyStreamJobCancelled() {
        InputStream inputStreamMock = mock(InputStream.class);
        OutputStream outputStreamMock = mock(OutputStream.class);

        baseFileConnector.copyStream(inputStreamMock, outputStreamMock, 123L, new WorkInProgressMonitor(),
                JobExecution.builder().executionState(JobExecutionState.CANCELLED).build());

        verifyNoInteractions(inputStreamMock);
    }

}
package com.arassec.igor.plugin.common.file.connector;

import com.arassec.igor.core.model.connector.BaseConnector;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.plugin.common.CommonCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Base for file connectors.
 */
@Getter
@Setter
@Slf4j
public abstract class BaseFileConnector extends BaseConnector implements FileConnector {

    /**
     * Configures the buffer size for copying streams.
     */
    private int streamCopyBufferSize = 1024 * 1024;

    /**
     * Creates a new component instance.
     *
     * @param typeId The type ID.
     */
    protected BaseFileConnector(String typeId) {
        super(CommonCategory.FILE.getId(), typeId);
    }

    /**
     * {@inheritDoc}
     */
    public void finalizeStream(FileStreamData fileStreamData) {
        // Nothing to do here by default...
    }

    /**
     * Copies the content of the input stream into the output stream.
     *
     * @param in                    The input stream.
     * @param out                   The output stream.
     * @param fileSize              The max number of bytes to copy.
     * @param workInProgressMonitor The {@link WorkInProgressMonitor} that keeps track of the copy progress.
     * @param jobExecution          The container for job execution data.
     */
    protected void copyStream(InputStream in, OutputStream out, long fileSize, WorkInProgressMonitor workInProgressMonitor,
                              JobExecution jobExecution) {
        try {
            long totalSize = fileSize;
            byte[] buf = new byte[streamCopyBufferSize];
            int foo;
            while (jobExecution.isRunningOrActive()) {
                if (buf.length < fileSize) {
                    foo = buf.length;
                } else {
                    foo = (int) fileSize;
                }
                foo = in.read(buf, 0, foo);
                if (foo < 0) {
                    // A negative return value indicates an error.
                    throw new IgorException("Could not copy data via streams!");
                }
                out.write(buf, 0, foo);
                fileSize -= foo;
                if (fileSize <= 0L) {
                    workInProgressMonitor.setProgressInPercent(100);
                    break;
                }
                out.flush();
                workInProgressMonitor.setProgressInPercent(calculatePercentage((totalSize - fileSize), totalSize));
            }
            out.flush();
        } catch (IOException e) {
            throw new IgorException("Could not copy data via streams!", e);
        }
    }

    /**
     * Formats an {@link Instant} as a String.
     *
     * @param instant The timestamp to format.
     *
     * @return The formatted time as String.
     */
    protected String formatInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Calculates the percentage.
     *
     * @param obtained The optained part of the total.
     * @param total    The total amount.
     *
     * @return The percentage.
     */
    private double calculatePercentage(long obtained, long total) {
        return (obtained * 100f) / total;
    }

}

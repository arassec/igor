package com.arassec.igor.module.file.service;

import com.arassec.igor.core.model.job.execution.WorkInProgressMonitor;
import com.arassec.igor.core.model.service.BaseService;
import com.arassec.igor.core.util.IgorException;
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
 * Base for file services.
 */
@Getter
@Setter
@Slf4j
public abstract class BaseFileService extends BaseService implements FileService {

    /**
     * Configures the buffer size for copying streams.
     */
    private int streamCopyBufferSize = 1024 * 1024;

    /**
     * Creates a new component instance.
     *
     * @param typeId The type ID.
     */
    public BaseFileService(String typeId) {
        super("file-connectors", typeId);
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
     */
    protected void copyStream(InputStream in, OutputStream out, long fileSize, WorkInProgressMonitor workInProgressMonitor) {
        try {
            long totalSize = fileSize;
            byte[] buf = new byte[streamCopyBufferSize];
            int foo;
            while (true) {
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
                workInProgressMonitor.setProgressInPercent(calculatePercentage(fileSize, totalSize));
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
    private double calculatePercentage(double obtained, double total) {
        return obtained * 100 / total;
    }

}

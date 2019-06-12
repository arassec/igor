package com.arassec.igor.module.file.service;

import com.arassec.igor.core.model.IgorParam;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.service.BaseService;
import com.arassec.igor.core.model.service.ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base for file services.
 */
public abstract class BaseFileService extends BaseService implements FileService {

    /**
     * Configures a timezone for the file service as String.
     */
    @IgorParam(optional = true)
    protected String timezone;

    /**
     * {@inheritDoc}
     */
    @Override
    public void finalizeStream(FileStreamData fileStreamData) {
        // Nothing to do here by default...
    }

    /**
     * Copies the content of the input stream into the output stream.
     *
     * @param in       The input stream.
     * @param out      The output stream.
     * @param fileSize The max number of bytes to copy.
     * @param jobExecution The job execution log.
     */
    protected void copyStream(InputStream in, OutputStream out, long fileSize, JobExecution jobExecution) {
        try {
            byte[] buf = new byte[1024 * 1024];
            int foo;
            while (true) {
                if (!jobExecution.isRunning()) {
                    break;
                }
                if (buf.length < fileSize) {
                    foo = buf.length;
                } else {
                    foo = (int) fileSize;
                }
                foo = in.read(buf, 0, foo);
                if (foo < 0) {
                    break; // error
                }
                out.write(buf, 0, foo);
                fileSize -= foo;
                if (fileSize == 0L) {
                    break;
                }
                out.flush();
            }
            out.flush();
        } catch (IOException e) {
            throw new ServiceException("Could not copy data via streams!", e);
        }
    }

    /**
     * Formats an {@link Instant} as a String.
     *
     * @param instant The timestamp to format.
     * @return The formatted time as String.
     */
    protected String formatInstant(Instant instant) {
        if (instant == null) {
            return null;
        }

        ZonedDateTime zonedDateTime;
        if (timezone != null && !timezone.isEmpty()) {
            zonedDateTime = instant.atZone(ZoneId.of(timezone));
        } else {
            zonedDateTime = instant.atZone(ZoneId.systemDefault());
        }

        return zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

}
